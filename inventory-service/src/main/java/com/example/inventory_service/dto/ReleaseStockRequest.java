package com.example.inventory_service.dto;

import java.util.UUID;

public record ReleaseStockRequest(
    UUID variantId,
    Integer quantity
) {
}
