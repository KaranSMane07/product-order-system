package com.example.shop.service;

import com.example.shop.dto.CartItemRequest;
import com.example.shop.dto.CartItemResponse;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.InsufficientStockException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CartItemRepository itemRepo;

    /** Returns the user's cart, creating it on first use. Must run inside a transaction. */
    private Cart getCart(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepo.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepo.save(c);
        });
    }

    private CartItemResponse map(CartItem i) {
        return new CartItemResponse(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                i.getProduct().getPrice(), i.getQuantity());
    }

    private List<CartItemResponse> mapAll(Cart cart) {
        return cart.getItems().stream().map(this::map).toList();
    }

    @Transactional
    public List<CartItemResponse> view(String email) {
        return mapAll(getCart(email));
    }

    @Transactional
    public List<CartItemResponse> addItem(String email, CartItemRequest r) {
        Cart cart = getCart(email);
        Product p = productRepo.findById(r.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + r.productId()));
        if (!p.isEnabled()) throw new BadRequestException("Product is not available");

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(p.getId()))
                .findFirst().orElse(null);

        int newQty = (item == null ? 0 : item.getQuantity()) + r.quantity();
        if (newQty > p.getQuantity())
            throw new InsufficientStockException("Only " + p.getQuantity() + " in stock for " + p.getName());

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(p);
            item.setQuantity(newQty);
            itemRepo.save(item);
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQty);
        }
        return mapAll(cart);
    }

    @Transactional
    public List<CartItemResponse> updateItem(String email, Long itemId, int quantity) {
        Cart cart = getCart(email);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        Product p = item.getProduct();
        if (!p.isEnabled()) throw new BadRequestException("Product is not available");
        if (quantity > p.getQuantity())
            throw new InsufficientStockException("Only " + p.getQuantity() + " in stock for " + p.getName());
        item.setQuantity(quantity);
        return mapAll(cart);
    }

    @Transactional
    public List<CartItemResponse> removeItem(String email, Long itemId) {
        Cart cart = getCart(email);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) throw new ResourceNotFoundException("Cart item not found: " + itemId);
        return mapAll(cart);
    }
}
