package com.example.product_service.mapper;

import com.example.product_service.dto.ProductAttributeDto;
import com.example.product_service.dto.ProductImageDto;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.ProductVariantDto;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductAttribute;
import com.example.product_service.entity.ProductImage;
import com.example.product_service.entity.ProductVariant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {
    public static ProductResponse toDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .slug(product.getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .status(product.getStatus())
                .images(product.getImages() != null ? 
                        product.getImages().stream().map(ProductMapper::toImageDto).collect(Collectors.toList()) : Collections.emptyList())
                .variants(product.getVariants() != null ? 
                        product.getVariants().stream().map(ProductMapper::toVariantDto).collect(Collectors.toList()) : Collections.emptyList())
                .attributes(product.getAttributes() != null ? 
                        product.getAttributes().stream().map(ProductMapper::toAttributeDto).collect(Collectors.toList()) : Collections.emptyList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static ProductImageDto toImageDto(ProductImage image) {
        if (image == null) return null;
        return ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .build();
    }

    public static ProductVariantDto toVariantDto(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantDto.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .size(variant.getSize())
                .price(variant.getPrice())
                .weight(variant.getWeight())
                .createdAt(variant.getCreatedAt())
                .build();
    }

    public static ProductAttributeDto toAttributeDto(ProductAttribute attribute) {
        if (attribute == null) return null;
        return ProductAttributeDto.builder()
                .id(attribute.getId())
                .attributeName(attribute.getAttributeName())
                .attributeValue(attribute.getAttributeValue())
                .build();
    }
}
