package com.example.inventory_service.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record AddInventoryRequest(
    @NotNull
    UUID productId,
    
    @NotNull
    UUID variantId,
    
    @NotNull
    Integer availableQuantity,
    
    Integer reservedQuantity,
    
    String warehouseLocation
) {
    
}
