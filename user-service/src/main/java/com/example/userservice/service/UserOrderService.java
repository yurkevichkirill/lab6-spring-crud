package com.example.userservice.service;

import com.example.userservice.client.OrderClient;
import com.example.userservice.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/** Сервисный слой клиента: обращается к Order-Service через Feign-клиент. */
@Service
public class UserOrderService {

    private final OrderClient orderClient;

    public UserOrderService(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    public List<Order> getUserOrders(String userId) {
        return orderClient.getOrdersByUserId(userId);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderClient.getOrdersByStatus(status);
    }
}
