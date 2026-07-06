package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private UUID id;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
}
