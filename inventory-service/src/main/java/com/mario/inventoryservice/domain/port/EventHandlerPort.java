package com.mario.inventoryservice.domain.port;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface EventHandlerPort {

    void handleReserveProductStockRequest(ConsumerRecord<String, String> record);

}
