package com.example.userservice.controller;

import com.example.userservice.model.Order;
import com.example.userservice.service.UserOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API user-service; данные получает из Order-Service через Feign. */
@RestController
public class UserController {

    private final UserOrderService userOrderService;

    public UserController(UserOrderService userOrderService) {
        this.userOrderService = userOrderService;
    }

    /** {@code GET /user/{id}/orders} → Feign → {@code GET /orders/{id}} на Order-Service. */
    @GetMapping("/user/{id}/orders")
    public List<Order> getUserOrders(@PathVariable("id") String id) {
        return userOrderService.getUserOrders(id);
    }

    /** {@code GET /user/orders?status=PAID} → Feign → {@code GET /orders?status=PAID}. */
    @GetMapping("/user/orders")
    public List<Order> getOrdersByStatus(@RequestParam("status") String status) {
        return userOrderService.getOrdersByStatus(status);
    }
}
