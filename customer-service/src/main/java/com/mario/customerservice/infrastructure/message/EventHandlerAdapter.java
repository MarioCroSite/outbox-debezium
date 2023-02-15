package com.mario.customerservice.infrastructure.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.customerservice.domain.PlacedOrderEvent;
import com.mario.customerservice.domain.port.CustomerUseCasePort;
import com.mario.customerservice.domain.port.EventHandlerPort;
import com.mario.customerservice.infrastructure.message.log.MessageLog;
import com.mario.customerservice.infrastructure.message.log.MessageLogRepository;
import com.mario.customerservice.infrastructure.message.outbox.OutBox;
import com.mario.customerservice.infrastructure.message.outbox.OutBoxRepository;
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

import static com.mario.customerservice.infrastructure.message.MessageStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerAdapter implements EventHandlerPort {

    private final ObjectMapper mapper;

    private final CustomerUseCasePort customerUseCasePort;

    private final OutBoxRepository outBoxRepository;

    private final MessageLogRepository messageLogRepository;

    @Override
    @KafkaListener(
            topics = {"ORDER.events"},
            containerFactory = "listenerContainer"
    )
    public void handleCustomerBalanceRequest(ConsumerRecord<String, String> record) {
        var key = record.key();
        var value = record.value();
        var eventType = getHeaderAsString(record.headers(), "eventType");

        if (!validEventType(eventType)) {
            log.debug("Ignoring event of type {}", eventType);
            return;
        }

//        if (messageLogRepository.existsById((UUID.fromString(key)))) {
//            log.debug("Message with ID {} has already been processed.", key);
//            return;
//        }

        switch (eventType) {
            case ORDER_CREATED -> orderCreated(value);
            case COMPENSATE_CUSTOMER_BALANCE -> compensateCustomerBalance(value);
        }

        // Marked message is processed
        //messageLogRepository.save(new MessageLog(UUID.fromString(key), Timestamp.from(Instant.now())));
    }

    private boolean validEventType(String eventType) {
        return eventType.equals(ORDER_CREATED) || eventType.equals(COMPENSATE_CUSTOMER_BALANCE);
    }


    private void orderCreated(String value) {
        var placedOrderEvent = deserialize(value);

        log.debug("Start process reserve customer balance {}", placedOrderEvent);
        var outbox = new OutBox();
        outbox.setAggregateId(placedOrderEvent.id());
        outbox.setPayload(mapper.convertValue(placedOrderEvent, JsonNode.class));
        outbox.setAggregateType(CUSTOMER);

        if (customerUseCasePort.reserveBalance(placedOrderEvent)) {
            outbox.setType(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY);
        } else {
            outbox.setType(RESERVE_CUSTOMER_BALANCE_FAILED);
        }

        // Exported event into outbox table
        outBoxRepository.save(outbox);
        log.debug("Done process reserve customer balance {}", placedOrderEvent);
    }

    private void compensateCustomerBalance(String value) {
        var placedOrderEvent = deserialize(value);

        log.debug("Start process compensate customer balance {}", placedOrderEvent);
        customerUseCasePort.compensateBalance(placedOrderEvent);
        log.debug("Done process compensate customer balance {}", placedOrderEvent);
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
