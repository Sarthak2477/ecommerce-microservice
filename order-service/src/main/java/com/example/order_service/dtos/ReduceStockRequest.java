package com.example.order_service.dtos;

import java.util.UUID;

public record ReduceStockRequest(
    UUID variantId,
    Integer quantity
) {
}
