package com.ecommerce_backend.cart_service.mapper;

import com.ecommerce_backend.cart_service.dto.response.CartItemResponse;
import com.ecommerce_backend.cart_service.dto.response.CartResponse;
import com.ecommerce_backend.cart_service.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(
            Cart cart,
            List<CartItemResponse> items) {

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                totalItems,
                total
        );
    }

    public CartResponse toEmptyResponse(Long userId) {

        return new CartResponse(
                null,
                userId,
                List.of(),
                0,
                BigDecimal.ZERO
        );
    }

}
