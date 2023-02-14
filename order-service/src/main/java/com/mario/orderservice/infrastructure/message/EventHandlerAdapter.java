package com.mario.orderservice.infrastructure.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.orderservice.domain.PlacedOrderEvent;
import com.mario.orderservice.domain.port.EventHandlerPort;
import com.mario.orderservice.domain.port.OrderUseCasePort;
import com.mario.orderservice.infrastructure.message.log.MessageLog;
import com.mario.orderservice.infrastructure.message.log.MessageLogRepository;
import com.mario.orderservice.infrastructure.message.outbox.OutBox;
import com.mario.orderservice.infrastructure.message.outbox.OutBoxRepository;
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

import static com.mario.orderservice.infrastructure.message.MessageStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerAdapter implements EventHandlerPort {

    private final ObjectMapper mapper;

    private final OrderUseCasePort orderUseCase;

    private final MessageLogRepository messageLogRepository;

    private final OutBoxRepository outBoxRepository;


    @Override
    @KafkaListener(
            topics = "CUSTOMER.events",
            containerFactory = "listenerContainer"
    )
    public void reserveCustomerBalanceStage(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = getHeaderAsString(record.headers(), "eventType");

        if(messageLogRepository.existsById((UUID.fromString(key)))) {
            log.debug("Message with ID {} has already been processed.", key);
            return;
        }

        switch (eventType) {
            case RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY -> reserveCustomerBalance(true, value);
            case RESERVE_CUSTOMER_BALANCE_FAILED -> reserveCustomerBalance(false, value);
            default -> log.debug("Ignoring event of type {}", eventType);
        }

        // Marked message is processed
        messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    @Override
    @KafkaListener(
            topics = "PRODUCT.events",
            containerFactory = "listenerContainer"
    )
    public void reserveProductStockStage(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = getHeaderAsString(record.headers(), "eventType");

        if(messageLogRepository.existsById((UUID.fromString(key)))) {
            log.debug("Message with ID {} has already been processed.", key);
            return;
        }

        switch (eventType) {
            case RESERVE_PRODUCT_STOCK_SUCCESSFULLY -> reserveProductStock(true, value);
            case RESERVE_PRODUCT_STOCK_FAILED -> reserveProductStock(false, value);
            default -> log.debug("Ignoring event of type {}", eventType);
        }

        // Marked message is processed
        messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    private void reserveCustomerBalance(boolean success, String value) {
        var placedOrderEvent = deserialize(value);

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
        var placedOrderEvent = deserialize(value);

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
