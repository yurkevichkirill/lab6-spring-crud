package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdAsc(String userId);

    List<Order> findByStatusOrderByIdAsc(OrderStatus status);
}
