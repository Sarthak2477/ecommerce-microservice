package com.example.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDescription;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Brand ID is required")
    private UUID brandId;

}