package com.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Представление заказа в ответах REST API (та же форма, что у {@code Order} в user-service). */
@Getter
@Setter
public class OrderDTO {
    private Long id;
    private String userId;
    private String product;
    private int quantity;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
