package com.example.userservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Заказ в том виде, в каком его возвращает Order-Service
 * (Feign десериализует JSON-ответ в этот record).
 */
public record Order(
        Long id,
        String userId,
        String product,
        int quantity,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt) {
}
