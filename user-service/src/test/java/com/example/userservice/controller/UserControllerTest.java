package com.example.userservice.controller;

import com.example.userservice.client.OrderClient;
import com.example.userservice.model.Order;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderClient orderClient;

    private final Faker faker = new Faker();

    private List<Order> orders(String userId, String status, int size) {
        return Instancio.ofList(Order.class).size(size)
                .set(Select.field(Order::userId), userId)
                .set(Select.field(Order::status), status)
                .supply(Select.field(Order::product), () -> faker.commerce().productName())
                .supply(Select.field(Order::amount),
                        () -> BigDecimal.valueOf(faker.number().randomDouble(2, 1, 5000)))
                .create();
    }

    @Test
    void testGetUserOrders() throws Exception {
        List<Order> expected = orders("42", "PAID", 2);
        when(orderClient.getOrdersByUserId("42")).thenReturn(expected);

        var body = mockMvc.perform(get("/user/{id}/orders", "42"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(2);
        assertThatJson(body).inPath("$[0]").and(
                v -> v.node("id").isEqualTo(expected.get(0).id()),
                v -> v.node("userId").isString().isEqualTo("42"),
                v -> v.node("product").isString().isEqualTo(expected.get(0).product()));
        verify(orderClient).getOrdersByUserId("42");
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        List<Order> expected = orders("3", "SHIPPED", 3);
        when(orderClient.getOrdersByStatus("SHIPPED")).thenReturn(expected);

        var body = mockMvc.perform(get("/user/orders").param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(3);
        assertThatJson(body).inPath("$[*].status").isArray().containsOnly("SHIPPED");
        verify(orderClient).getOrdersByStatus("SHIPPED");
    }

    @Test
    void testGetOrdersByStatusWithoutParamIsBadRequest() throws Exception {
        mockMvc.perform(get("/user/orders")).andExpect(status().isBadRequest());
    }
}
