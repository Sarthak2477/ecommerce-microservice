package com.example.inventory_service.dto;

import java.util.UUID;

public record ReduceStockRequest(
    @org.hibernate.validator.constraints.UUID
    UUID variantId,
    
    Integer quantity
) {
   
}
