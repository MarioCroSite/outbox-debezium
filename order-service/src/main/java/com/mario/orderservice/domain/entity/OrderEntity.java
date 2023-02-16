package com.mario.orderservice.domain.entity;

import com.mario.orderservice.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "orders")
@Builder
public class OrderEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID productId;

    private BigDecimal price;

    private int quantity;

    private Timestamp createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

}
