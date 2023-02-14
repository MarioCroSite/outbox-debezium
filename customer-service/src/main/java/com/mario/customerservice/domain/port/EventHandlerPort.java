package com.mario.customerservice.domain.port;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface EventHandlerPort {

    void handleCustomerBalanceRequest(ConsumerRecord<String, String> consumerRecord);

}
