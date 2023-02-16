package com.mario.orderservice.domain.mapper;

import com.mario.orderservice.model.requests.OrderRequest;
import com.mario.orderservice.model.Order;
import com.mario.orderservice.domain.entity.OrderEntity;

public class OrderMapper {

    private OrderMapper() {

    }
    public static Order toOrder(OrderRequest orderRequest) {
        return Order.builder()
                .customerId(orderRequest.customerId())
                .productId(orderRequest.productId())
                .quantity(orderRequest.quantity())
                .price(orderRequest.price())
                .build();
    }


    public static OrderEntity toEntity(Order order) {
        return OrderEntity.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .build();
    }

    public static Order toOrder(OrderEntity orderEntity) {
        return Order.builder()
                .id(orderEntity.getId())
                .customerId(orderEntity.getCustomerId())
                .productId(orderEntity.getProductId())
                .price(orderEntity.getPrice())
                .quantity(orderEntity.getQuantity())
                .createdAt(orderEntity.getCreatedAt())
                .status(orderEntity.getStatus())
                .build();
    }

}
