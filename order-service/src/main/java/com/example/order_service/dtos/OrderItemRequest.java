package com.example.order_service.dtos;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.UUID;

import io.micrometer.common.lang.NonNull;

public record OrderItemRequest(
    @UUID
    java.util.UUID id,
    @NonNull
    Integer quantity,   
    @NonNull
    BigDecimal price,
    @NonNull
    BigDecimal subtotal
) {
    
}
