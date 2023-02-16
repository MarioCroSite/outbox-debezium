package com.mario.inventoryservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "products")
@Builder
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private int stocks;

}
