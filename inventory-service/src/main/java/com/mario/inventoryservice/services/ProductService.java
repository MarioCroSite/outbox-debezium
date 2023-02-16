package com.mario.inventoryservice.services;

import com.mario.common.PlacedOrderEvent;
import com.mario.inventoryservice.domain.mapper.ProductMapper;
import com.mario.inventoryservice.model.Product;
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

    public Product findById(UUID productId) {
        return productRepository
                .findById(productId).map(ProductMapper::toProduct)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Product create(ProductRequest productRequest) {
        var product = ProductMapper.toProduct(productRequest);
        product.setId(UUID.randomUUID());
        productRepository.save(ProductMapper.toEntity(product));
        return product;
    }

    @Transactional
    public boolean reserveProduct(PlacedOrderEvent orderEvent) {
        var product = findById(orderEvent.productId());
        if (product.getStocks() - orderEvent.quantity() < 0) {
            return false;
        }
        product.setStocks(product.getStocks() - orderEvent.quantity());
        productRepository.save(ProductMapper.toEntity(product));
        return true;
    }

}
