package com.mario.inventoryservice.infrastructure.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.common.EventHelper;
import com.mario.inventoryservice.domain.port.EventHandlerPort;
import com.mario.inventoryservice.domain.port.ProductUseCasePort;
import com.mario.inventoryservice.infrastructure.message.log.MessageLogRepository;
import com.mario.inventoryservice.infrastructure.message.outbox.OutBox;
import com.mario.inventoryservice.infrastructure.message.outbox.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mario.inventoryservice.infrastructure.message.MessageStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerAdapter implements EventHandlerPort {

    private final ObjectMapper mapper;

    private final ProductUseCasePort productUseCase;

    private final MessageLogRepository messageLogRepository;

    private final OutBoxRepository outBoxRepository;

    private final EventHelper eventHelper;


    @Override
    @KafkaListener(
            topics = {"ORDER.events"},
            containerFactory = "listenerContainer"
    )
    public void handleReserveProductStockRequest(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = eventHelper.getHeaderAsString(record.headers(), "eventType");

        if (!validEventType(eventType)) {
            log.debug("Ignoring event of type {}", eventType);
            return;
        }

//        if (messageLogRepository.existsById((UUID.fromString(key)))) {
//            log.debug("Message with ID {} has already been processed.", key);
//            return;
//        }

        if (eventType.equals(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY)) {
            processReserveProduct(value);
        }

        // Marked message is processed
        //messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    private boolean validEventType(String eventType) {
        return eventType.equals(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY);
    }

    private void processReserveProduct(String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        log.debug("Start process reserve product stock {}", placedOrderEvent);
        var outbox = new OutBox();
        outbox.setAggregateId(placedOrderEvent.id());
        outbox.setAggregateType(PRODUCT);
        outbox.setPayload(mapper.convertValue(placedOrderEvent, JsonNode.class));

        if (productUseCase.reserveProduct(placedOrderEvent)) {
            outbox.setType(RESERVE_PRODUCT_STOCK_SUCCESSFULLY);
        } else {
            outbox.setType(RESERVE_PRODUCT_STOCK_FAILED);
        }

        // Exported event into outbox table
        outBoxRepository.save(outbox);
        log.debug("Done process reserve product stock {}", placedOrderEvent);
    }


}
