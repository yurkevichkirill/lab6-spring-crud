package com.example.orderservice.mapper;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class OrderMapper {

    public abstract OrderDTO map(Order order);

    public abstract List<OrderDTO> map(List<Order> orders);
}
