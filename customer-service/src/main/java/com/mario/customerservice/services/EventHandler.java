package com.mario.customerservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.common.EventHelper;
import com.mario.customerservice.repositories.MessageLogRepository;
import com.mario.customerservice.domain.entity.OutBox;
import com.mario.customerservice.repositories.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mario.customerservice.util.MessageStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHandler {

    private final ObjectMapper mapper;

    private final CustomerService customerService;

    private final OutBoxRepository outBoxRepository;

    private final MessageLogRepository messageLogRepository;

    private final EventHelper eventHelper;

    @KafkaListener(
            topics = {"ORDER.events"},
            containerFactory = "listenerContainer"
    )
    public void handleCustomerBalance(ConsumerRecord<String, String> record) {
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

    @Transactional
    public void orderCreated(String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        log.debug("Start process reserve customer balance {}", placedOrderEvent);
        var outbox = new OutBox();
        outbox.setAggregateId(placedOrderEvent.id());
        outbox.setPayload(mapper.convertValue(placedOrderEvent, JsonNode.class));
        outbox.setAggregateType(CUSTOMER);

        if (customerService.reserveBalance(placedOrderEvent)) {
            outbox.setType(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY);
        } else {
            outbox.setType(RESERVE_CUSTOMER_BALANCE_FAILED);
        }

        // Exported event into outbox table
        outBoxRepository.save(outbox);
        log.debug("Done process reserve customer balance {}", placedOrderEvent);
    }

    @Transactional
    public void compensateCustomerBalance(String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        log.debug("Start process compensate customer balance {}", placedOrderEvent);
        customerService.compensateBalance(placedOrderEvent);
        log.debug("Done process compensate customer balance {}", placedOrderEvent);
    }

}
