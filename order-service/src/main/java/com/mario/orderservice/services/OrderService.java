package com.mario.orderservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.orderservice.domain.entity.OutBox;
import com.mario.orderservice.model.OrderStatus;
import com.mario.orderservice.domain.mapper.OrderMapper;
import com.mario.orderservice.model.requests.OrderRequest;
import com.mario.orderservice.repositories.OrderRepository;
import com.mario.orderservice.repositories.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static com.mario.orderservice.util.MessageStatus.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OutBoxRepository outBoxRepository;

    private final ObjectMapper mapper;

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        var order = OrderMapper.toOrderEntity(orderRequest);

        order.setCreatedAt(Timestamp.from(Instant.now()));
        order.setStatus(OrderStatus.PENDING);
        order.setId(UUID.randomUUID());

        orderRepository.save(order);
        outBoxRepository.save(OutBox.builder()
                .aggregateId(order.getId())
                .aggregateType(ORDER)
                .type(ORDER_CREATED)
                .payload(mapper.convertValue(order, JsonNode.class))
                .build());
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, boolean success) {
        var order = orderRepository.findById(orderId);

        if (order.isPresent()) {
            if (success) {
                order.get().setStatus(OrderStatus.COMPLETED);
            } else {
                order.get().setStatus(OrderStatus.CANCELED);
            }
            orderRepository.save(order.get());
        }
    }


}
