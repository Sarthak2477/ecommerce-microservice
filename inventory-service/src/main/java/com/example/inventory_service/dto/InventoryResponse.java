package com.example.inventory_service.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private UUID id;
    private UUID variantId;
    private UUID productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String warehouseLocation;
}
