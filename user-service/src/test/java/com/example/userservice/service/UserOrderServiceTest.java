package com.example.userservice.service;

import com.example.userservice.client.OrderClient;
import com.example.userservice.model.Order;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceTest {

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private UserOrderService service;

    @Test
    void getUserOrdersDelegatesToFeignClient() {
        List<Order> orders = Instancio.ofList(Order.class).size(2).create();
        when(orderClient.getOrdersByUserId("1")).thenReturn(orders);

        assertThat(service.getUserOrders("1")).isEqualTo(orders);
        verify(orderClient).getOrdersByUserId("1");
    }

    @Test
    void getOrdersByStatusDelegatesToFeignClient() {
        when(orderClient.getOrdersByStatus("NEW")).thenReturn(List.of());

        assertThat(service.getOrdersByStatus("NEW")).isEmpty();
        verify(orderClient).getOrdersByStatus("NEW");
    }
}
