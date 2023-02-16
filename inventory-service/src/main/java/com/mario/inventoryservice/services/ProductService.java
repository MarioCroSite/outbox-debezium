package com.mario.inventoryservice.services;

import com.mario.common.PlacedOrderEvent;
import com.mario.inventoryservice.domain.entity.ProductEntity;
import com.mario.inventoryservice.domain.mapper.ProductMapper;
import com.mario.inventoryservice.domain.exception.NotFoundException;
import com.mario.inventoryservice.model.requests.ProductRequest;
import com.mario.inventoryservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductEntity findById(UUID productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public ProductEntity create(ProductRequest productRequest) {
        var product = ProductMapper.toEntity(productRequest);
        product.setId(UUID.randomUUID());
        return productRepository.save(product);
    }

    @Transactional
    public boolean reserveProduct(PlacedOrderEvent orderEvent) {
        var product = findById(orderEvent.productId());
        var currentStock = product.getStocks() - orderEvent.quantity();
        if (currentStock < 0) {
            return false;
        }
        product.setStocks(currentStock);
        productRepository.save(product);
        return true;
    }

}
