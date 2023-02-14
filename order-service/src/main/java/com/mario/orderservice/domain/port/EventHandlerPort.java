package com.mario.orderservice.domain.port;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface EventHandlerPort {

    void reserveCustomerBalanceStage(ConsumerRecord<String, String> record);

    void reserveProductStockStage(ConsumerRecord<String, String> record);
}
