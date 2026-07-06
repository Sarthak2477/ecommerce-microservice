package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto {
    private UUID id;
    private String sku;
    private String variantName;
    private String color;
    private String size;
    private BigDecimal price;
    private BigDecimal weight;
    private LocalDateTime createdAt;
}
