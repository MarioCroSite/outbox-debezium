package com.mario.orderservice.domain.port;

import com.mario.orderservice.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Optional<Order> findOrderById(UUID orderId);

    void saveOrder(Order order);

    void exportOutBoxEvent(Order order);

}
