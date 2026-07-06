package com.example.inventory_service.dto;

import java.util.UUID;

public record ReserveStockRequest(
    UUID variantId,
    Integer quantity
) {
}
