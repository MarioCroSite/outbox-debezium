package com.mario.inventoryservice.domain.port;

import com.mario.common.PlacedOrderEvent;
import com.mario.inventoryservice.domain.ProductRequest;
import com.mario.inventoryservice.domain.entity.Product;

import java.util.UUID;

public interface ProductUseCasePort {

    Product findById(UUID productId);

    Product create(ProductRequest productRequest);

    boolean reserveProduct(PlacedOrderEvent orderEvent);
}
