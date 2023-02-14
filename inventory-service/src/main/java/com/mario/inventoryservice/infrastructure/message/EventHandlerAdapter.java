package com.mario.inventoryservice.infrastructure.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.inventoryservice.domain.PlacedOrderEvent;
import com.mario.inventoryservice.domain.port.EventHandlerPort;
import com.mario.inventoryservice.domain.port.ProductUseCasePort;
import com.mario.inventoryservice.infrastructure.message.log.MessageLog;
import com.mario.inventoryservice.infrastructure.message.log.MessageLogRepository;
import com.mario.inventoryservice.infrastructure.message.outbox.OutBox;
import com.mario.inventoryservice.infrastructure.message.outbox.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static com.mario.inventoryservice.infrastructure.message.MessageStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerAdapter implements EventHandlerPort {

    private final ObjectMapper mapper;

    private final ProductUseCasePort productUseCase;

    private final MessageLogRepository messageLogRepository;

    private final OutBoxRepository outBoxRepository;


    @Override
    @KafkaListener(
            topics = "ORDER.events",
            containerFactory = "listenerContainer"
    )
    public void handleReserveProductStockRequest(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = getHeaderAsString(record.headers(), "eventType");

        if(messageLogRepository.existsById((UUID.fromString(key)))) {
            log.debug("Message with ID {} has already been processed.", key);
            return;
        }

        switch (eventType) {
            case RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY -> processReserveProduct(value);
            default -> log.debug("Ignoring event of type {}", eventType);
        }

        // Marked message is processed
        messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    private void processReserveProduct(String value) {
        var placedOrderEvent = deserialize(value);

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

    private PlacedOrderEvent deserialize(String event) {
        PlacedOrderEvent placedOrderEvent;
        try {
            String unescaped = mapper.readValue(event, String.class);
            placedOrderEvent = mapper.readValue(unescaped, PlacedOrderEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't deserialize event", e);
        }
        return placedOrderEvent;
    }

    private String getHeaderAsString(Headers headers, String name) {
        var value = headers.lastHeader(name);
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException(
                    String.format("Expected record header %s not present", name));
        }
        return new String(value.value(), StandardCharsets.UTF_8);
    }

}
