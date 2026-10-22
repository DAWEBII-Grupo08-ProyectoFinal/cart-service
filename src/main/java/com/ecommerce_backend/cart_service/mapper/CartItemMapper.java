package com.ecommerce_backend.cart_service.mapper;

import com.ecommerce_backend.cart_service.dto.response.CartItemResponse;
import com.ecommerce_backend.cart_service.dto.response.ProductResponse;
import com.ecommerce_backend.cart_service.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemMapper {

    public CartItemResponse toResponse(
            CartItem item,
            ProductResponse product) {

        BigDecimal subtotal = product.price()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                product.id(),
                product.name(),
                product.imageUrl(),
                product.price(),
                item.getQuantity(),
                subtotal
        );
    }

}
