package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.exception.InvalidOrderStatusException;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Логика выборки заказов; контроллер только делегирует сюда. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    /** Все заказы (для справки). */
    public List<OrderDTO> getAll() {
        return mapper.map(repository.findAll());
    }

    /** Заказы пользователя; если заказов нет — пустой список. */
    public List<OrderDTO> getByUserId(String userId) {
        return mapper.map(repository.findByUserIdOrderByIdAsc(userId));
    }

    /** Заказы с указанным статусом (регистр не важен: {@code paid} = {@code PAID}). */
    public List<OrderDTO> getByStatus(String status) {
        return mapper.map(repository.findByStatusOrderByIdAsc(parseStatus(status)));
    }

    static OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidOrderStatusException("Unknown order status: " + status
                    + ". Allowed: " + Arrays.toString(OrderStatus.values()));
        }
    }
}
