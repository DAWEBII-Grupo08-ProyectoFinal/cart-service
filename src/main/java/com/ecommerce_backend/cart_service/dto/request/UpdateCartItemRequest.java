package com.ecommerce_backend.cart_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(
        @NotNull
        @Positive
        Integer quantity
) {
}
