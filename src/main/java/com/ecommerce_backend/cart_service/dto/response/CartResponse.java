package com.ecommerce_backend.cart_service.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal total
) {
}
