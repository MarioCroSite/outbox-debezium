package com.mario.inventoryservice.model.requests;

import jakarta.validation.constraints.NotBlank;

public record ProductRequest(@NotBlank String name, int stocks) {

}
