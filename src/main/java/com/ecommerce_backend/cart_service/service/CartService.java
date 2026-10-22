package com.ecommerce_backend.cart_service.service;

import com.ecommerce_backend.cart_service.client.CatalogClient;
import com.ecommerce_backend.cart_service.dto.request.AddCartItemRequest;
import com.ecommerce_backend.cart_service.dto.request.UpdateCartItemRequest;
import com.ecommerce_backend.cart_service.dto.response.CartItemResponse;
import com.ecommerce_backend.cart_service.dto.response.CartResponse;
import com.ecommerce_backend.cart_service.dto.response.ProductResponse;
import com.ecommerce_backend.cart_service.entity.Cart;
import com.ecommerce_backend.cart_service.entity.CartItem;
import com.ecommerce_backend.cart_service.mapper.CartItemMapper;
import com.ecommerce_backend.cart_service.mapper.CartMapper;
import com.ecommerce_backend.cart_service.repository.CartItemRepository;
import com.ecommerce_backend.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final CatalogClient catalogClient;

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        // Si el usuario no tiene un carrito asignado, se crea uno vacio
        if (cart == null) {
            return cartMapper.toEmptyResponse(userId);
        }

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item: items) {
            ProductResponse product = catalogClient.getProductById(item.getProductId());
            itemResponses.add(cartItemMapper.toResponse(item, product));
        }

        return cartMapper.toResponse(cart, itemResponses);

    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        catalogClient.getProductById(request.productId());

        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        // Si el usuario no tiene un carrito, lo creamos
        if (cart == null) {
            cart = new Cart();

            cart.setUserId(userId);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());

            cart = cartRepository.save(cart);
        }

        // Buscamos la existencia de un producto dentro de un carrito
        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());

        // Si existe, actualizamos la cantidad
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());

            cartItemRepository.save(item);
        // Si no existe, creamos un CartItem del producto
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(request.productId());
            item.setQuantity(request.quantity());

            cartItemRepository.save(item);
        }

        // Actualizamos y guardamos
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long productId, UpdateCartItemRequest request) {
        // Buscamos el carrito
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        // Buscamos el item
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el carrito"));

        // Actualizamos la cantidad del item
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        // Registramos momento de actualizacion y guardamos
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        // Buscamos el carrito
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        // Buscamos el item
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el carrito"));

        // Eliminamos el item
        cartItemRepository.delete(item);

        // Registramos momento de actualizacion y guardamos
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        if (cart == null) {
           return;
        }

        cartItemRepository.deleteByCartId(cart.getId());
        cart.setUpdatedAt(LocalDateTime.now());
    }

}
