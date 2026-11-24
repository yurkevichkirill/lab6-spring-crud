package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository repository;

    private final Faker faker = new Faker();

    private String userId;
    private List<Order> saved;

    private Order newOrder(String user, OrderStatus status) {
        return Instancio.of(Order.class)
                .ignore(Select.field(Order::getId))
                .ignore(Select.field(Order::getCreatedAt))
                .set(Select.field(Order::getUserId), user)
                .set(Select.field(Order::getStatus), status)
                .supply(Select.field(Order::getProduct), () -> faker.commerce().productName())
                .supply(Select.field(Order::getQuantity), () -> faker.number().numberBetween(1, 10))
                .supply(Select.field(Order::getAmount),
                        () -> BigDecimal.valueOf(faker.number().randomDouble(2, 1, 5000)))
                .create();
    }

    @BeforeEach
    void setUp() {
        userId = "test-" + UUID.randomUUID();
        saved = repository.saveAll(List.of(
                newOrder(userId, OrderStatus.PAID),
                newOrder(userId, OrderStatus.NEW)));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll(saved);
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        var body = mockMvc.perform(get("/orders/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Order first = saved.get(0);
        assertThatJson(body).isArray().hasSize(2);
        assertThatJson(body).inPath("$[0]").and(
                v -> v.node("id").isEqualTo(first.getId()),
                v -> v.node("userId").isString().isEqualTo(userId),
                v -> v.node("product").isString().isEqualTo(first.getProduct()),
                v -> v.node("quantity").isEqualTo(first.getQuantity()),
                v -> v.node("status").isEqualTo("PAID"),
                v -> v.node("createdAt").isPresent());
        assertThatJson(body).inPath("$[1].status").isEqualTo("NEW");
    }

    @Test
    void testGetOrdersByUnknownUserReturnsEmptyList() throws Exception {
        var body = mockMvc.perform(get("/orders/{userId}", "no-such-user"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThatJson(body).isArray().isEmpty();
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        var body = mockMvc.perform(get("/orders").param("status", "paid"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().isNotEmpty();
        assertThatJson(body).inPath("$[*].status").isArray().containsOnly("PAID");
        assertThatJson(body).inPath("$[*].userId").isArray().contains(userId);
    }

    @Test
    void testGetOrdersByInvalidStatus() throws Exception {
        mockMvc.perform(get("/orders").param("status", "LOST"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllOrders() throws Exception {
        var body = mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // 6 демо-заказов из data.sql + 2 созданных в тесте
        assertThatJson(body).isArray().hasSizeGreaterThanOrEqualTo(8);
        assertThatJson(body).inPath("$[?(@.userId == '1')]").isArray().hasSize(3);
        assertThatJson(body).inPath("$[?(@.userId == '%s')]".formatted(userId)).isArray().hasSize(2);
    }
}
