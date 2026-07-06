package com.example.order_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.order_service.dtos.CreateOrderRequest;
import com.example.order_service.dtos.OrderItemRequest;
import com.example.order_service.dtos.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderid", source = "id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toEntity(CreateOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", source = "id")
    OrderItem toEntity(OrderItemRequest request);
}
