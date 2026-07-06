package com.example.order_service.dtos;

import java.util.List;

public record CreateOrderRequest(
    List<OrderItemRequest> items
) {
    
}
