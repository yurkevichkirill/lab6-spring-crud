package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.exception.InvalidOrderStatusException;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderService service;

    @Test
    void getByUserIdDelegatesToRepository() {
        List<Order> orders = Instancio.ofList(Order.class).size(2).create();
        List<OrderDTO> dtos = Instancio.ofList(OrderDTO.class).size(2).create();
        when(repository.findByUserIdOrderByIdAsc("1")).thenReturn(orders);
        when(mapper.map(orders)).thenReturn(dtos);

        assertThat(service.getByUserId("1")).isSameAs(dtos);
        verify(repository).findByUserIdOrderByIdAsc("1");
    }

    @Test
    void getByStatusParsesStatusCaseInsensitive() {
        when(repository.findByStatusOrderByIdAsc(OrderStatus.SHIPPED)).thenReturn(List.of());
        when(mapper.map(List.<Order>of())).thenReturn(List.of());

        assertThat(service.getByStatus(" shipped ")).isEmpty();
        verify(repository).findByStatusOrderByIdAsc(OrderStatus.SHIPPED);
    }

    @Test
    void getByStatusRejectsUnknownStatus() {
        assertThatThrownBy(() -> service.getByStatus("LOST"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Unknown order status: LOST");
        verifyNoInteractions(repository);
    }

    @Test
    void getAllReturnsMappedOrders() {
        List<Order> orders = Instancio.ofList(Order.class).size(3).create();
        when(repository.findAll()).thenReturn(orders);
        when(mapper.map(any(List.class))).thenReturn(List.of(new OrderDTO()));

        assertThat(service.getAll()).hasSize(1);
        verify(repository).findAll();
    }
}
