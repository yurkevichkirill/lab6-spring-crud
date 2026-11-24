package com.example.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Заказ пользователя (таблица {@code orders}: слово ORDER зарезервировано в SQL). */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(name = "user_id", nullable = false)
    @ToString.Include
    private String userId;

    @NotBlank
    @ToString.Include
    private String product;

    @Min(1)
    private int quantity;

    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private OrderStatus status;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
