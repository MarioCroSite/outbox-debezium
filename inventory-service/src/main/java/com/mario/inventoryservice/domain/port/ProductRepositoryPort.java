package com.mario.inventoryservice.domain.port;

import com.mario.inventoryservice.domain.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    Optional<Product> findProductById(UUID productId);

    Product saveProduct(Product product);
}
