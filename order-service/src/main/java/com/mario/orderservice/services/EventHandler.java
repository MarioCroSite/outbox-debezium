package com.mario.orderservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.common.EventHelper;
import com.mario.orderservice.repositories.MessageLogRepository;
import com.mario.orderservice.domain.entity.OutBox;
import com.mario.orderservice.repositories.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mario.orderservice.util.MessageStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHandler {

    private final ObjectMapper mapper;

    private final OrderService orderService;

    private final MessageLogRepository messageLogRepository;

    private final OutBoxRepository outBoxRepository;

    private final EventHelper eventHelper;


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

    @Transactional
    public void reserveCustomerBalance(boolean success, String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        if(success) {
            outBoxRepository.save(OutBox.builder()
                    .aggregateId(placedOrderEvent.id())
                    .payload(mapper.convertValue(placedOrderEvent, JsonNode.class))
                    .aggregateType(ORDER)
                    .type(RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY)
                    .build());
        } else {
            orderService.updateOrderStatus(placedOrderEvent.id(), false);
        }
    }

    @Transactional
    public void reserveProductStock(boolean success, String value) {
        var placedOrderEvent = eventHelper.deserialize(value);

        if(success) {
            orderService.updateOrderStatus(placedOrderEvent.id(), true);
        } else {
            orderService.updateOrderStatus(placedOrderEvent.id(), false);
            outBoxRepository.save(OutBox.builder()
                    .aggregateId(placedOrderEvent.id())
                    .aggregateType(ORDER)
                    .type(COMPENSATE_CUSTOMER_BALANCE)
                    .payload(mapper.convertValue(placedOrderEvent, JsonNode.class))
                    .build());
        }
    }

}
