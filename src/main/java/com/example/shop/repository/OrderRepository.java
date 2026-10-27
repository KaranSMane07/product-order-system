package com.example.shop.repository;

import com.example.shop.entity.Order;
import com.example.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByIdDesc(User user);
}
