package com.example.product_service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDescription;

    private UUID categoryId;

    private UUID brandId;

}