package com.mario.orderservice.domain.port;

import com.mario.orderservice.domain.OrderRequest;

import java.util.UUID;

public interface OrderUseCasePort {
    void placeOrder(OrderRequest orderRequest);

    void updateOrderStatus(UUID orderId, boolean success);
}
