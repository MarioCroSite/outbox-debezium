package com.mario.orderservice.infrastructure.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.common.EventHelper;
import com.mario.orderservice.domain.port.EventHandlerPort;
import com.mario.orderservice.domain.port.OrderUseCasePort;
import com.mario.orderservice.infrastructure.message.log.MessageLogRepository;
import com.mario.orderservice.infrastructure.message.outbox.OutBox;
import com.mario.orderservice.infrastructure.message.outbox.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mario.orderservice.infrastructure.message.MessageStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerAdapter implements EventHandlerPort {

    private final ObjectMapper mapper;

    private final OrderUseCasePort orderUseCase;

    private final MessageLogRepository messageLogRepository;

    private final OutBoxRepository outBoxRepository;

    private final EventHelper eventHelper;


    @Override
    @KafkaListener(
            topics = {"CUSTOMER.events"},
            containerFactory = "listenerContainer"
    )
    public void reserveCustomerBalanceStage(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = eventHelper.getHeaderAsString(record.headers(), "eventType");

//        if(messageLogRepository.existsById((UUID.fromString(key)))) {
//            log.debug("Message with ID {} has already been processed.", key);
//            return;
//        }

        switch (eventType) {
            case RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY -> reserveCustomerBalance(true, value);
            case RESERVE_CUSTOMER_BALANCE_FAILED -> reserveCustomerBalance(false, value);
            default -> log.debug("Ignoring event of type {}", eventType);
        }

        // Marked message is processed
        //messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    @Override
    @KafkaListener(
            topics = {"PRODUCT.events"},
            containerFactory = "listenerContainer"
    )
    public void reserveProductStockStage(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = eventHelper.getHeaderAsString(record.headers(), "eventType");

//        if(messageLogRepository.existsById((UUID.fromString(key)))) {
//            log.debug("Message with ID {} has already been processed.", key);
//            return;
//        }

        switch (eventType) {
            case RESERVE_PRODUCT_STOCK_SUCCESSFULLY -> reserveProductStock(true, value);
            case RESERVE_PRODUCT_STOCK_FAILED -> reserveProductStock(false, value);
            default -> log.debug("Ignoring event of type {}", eventType);
        }

        // Marked message is processed
        //messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    private void reserveCustomerBalance(boolean success, String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        if(success) {
            var outbox =
                    OutBox.builder()
                            .aggregateId(placedOrderEvent.id())
                            .payload(mapper.convertValue(placedOrderEvent, JsonNode.class))
                            .aggregateType(ORDER)
                            .type(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY)
                            .build();
            outBoxRepository.save(outbox);
        } else {
            orderUseCase.updateOrderStatus(placedOrderEvent.id(), false);
        }
    }

    private void reserveProductStock(boolean success, String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        if(success) {
            orderUseCase.updateOrderStatus(placedOrderEvent.id(), true);
        } else {
            orderUseCase.updateOrderStatus(placedOrderEvent.id(), false);
            var outbox =
                    OutBox.builder()
                            .aggregateId(placedOrderEvent.id())
                            .aggregateType(ORDER)
                            .type(COMPENSATE_CUSTOMER_BALANCE)
                            .payload(mapper.convertValue(placedOrderEvent, JsonNode.class))
                            .build();
            outBoxRepository.save(outbox);
        }
    }

}
