package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API заказов Order-Service. */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** {@code GET /orders/{userId}} — заказы пользователя. */
    @GetMapping("/{userId}")
    public List<OrderDTO> getOrdersByUserId(@PathVariable("userId") String userId) {
        return orderService.getByUserId(userId);
    }

    /**
     * {@code GET /orders?status=PAID} — заказы с указанным статусом;
     * без параметра — все заказы.
     */
    @GetMapping
    public List<OrderDTO> getOrders(@RequestParam(value = "status", required = false) String status) {
        return status == null ? orderService.getAll() : orderService.getByStatus(status);
    }
}
