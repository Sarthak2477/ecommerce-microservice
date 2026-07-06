package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;
    private String sku;
    private String slug;
    private String name;
    private String description;
    private String shortDescription;
    private UUID categoryId;
    private UUID brandId;
    private String status;
    private List<ProductImageDto> images;
    private List<ProductVariantDto> variants;
    private List<ProductAttributeDto> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}