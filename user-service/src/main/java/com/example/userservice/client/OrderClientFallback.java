package com.example.userservice.client;

import com.example.userservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Механизм резервирования: вызывается Spring Cloud CircuitBreaker (Resilience4j),
 * когда Order-Service недоступен или отвечает ошибкой. Возвращает пустой список.
 */
@Component
public class OrderClientFallback implements OrderClient {

    private static final Logger LOG = LoggerFactory.getLogger(OrderClientFallback.class);

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        LOG.warn("Order-Service unavailable: fallback for getOrdersByUserId({}) -> []", userId);
        return Collections.emptyList();
    }

    @Override
    public List<Order> getOrdersByStatus(String orderStatus) {
        LOG.warn("Order-Service unavailable: fallback for getOrdersByStatus({}) -> []", orderStatus);
        return Collections.emptyList();
    }
}
