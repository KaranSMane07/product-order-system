package com.example.shop.service;

import com.example.shop.dto.OrderItemResponse;
import com.example.shop.dto.OrderResponse;
import com.example.shop.entity.*;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.InsufficientStockException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    /**
     * Places an order from the user's cart.
     * Everything runs in ONE transaction: if any item fails (stock, disabled product, ...)
     * an exception is thrown and Spring rolls back, so no stock is reduced and no order is saved.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse placeOrder(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            // lock the product row so concurrent orders cannot oversell
            Product p = productRepo.findByIdForUpdate(ci.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (!p.isEnabled())
                throw new BadRequestException(p.getName() + " is no longer available");
            if (p.getQuantity() < ci.getQuantity())
                throw new InsufficientStockException("Insufficient stock for " + p.getName()
                        + " (available: " + p.getQuantity() + ", requested: " + ci.getQuantity() + ")");

            p.setQuantity(p.getQuantity() - ci.getQuantity()); // reduce inventory

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice()); // price snapshot
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotalAmount(total);
        orderRepo.save(order);
        cart.getItems().clear(); // empty the cart after a successful order
        return map(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepo.findByUserOrderByIdDesc(user).stream().map(this::map).toList();
    }

    private OrderResponse map(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getPrice()))
                .toList();
        return new OrderResponse(o.getId(), o.getTotalAmount(), o.getStatus().name(), o.getCreatedAt(), items);
    }
}
