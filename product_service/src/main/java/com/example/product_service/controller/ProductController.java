package com.example.product_service.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductAttributeDto;
import com.example.product_service.dto.ProductImageDto;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.ProductVariantDto;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.service.ProductService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(){
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<ProductResponse>> getProduct(@PathVariable @NonNull UUID id){
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping()
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest productRequest){
        ProductResponse productResponse = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id,@Valid @RequestBody UpdateProductRequest productRequest){
        return ResponseEntity.ok(productService.updateProduct(id, productRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    } 

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImageDto>> getProductImages(@PathVariable UUID id){
        return ResponseEntity.ok(productService.getImages(id));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductResponse> addProductImage(@PathVariable UUID id, @Valid @RequestBody ProductImageDto imageDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addImage(id, imageDto));
    }

    @PutMapping("/{id}/images/{imageId}")
    public ResponseEntity<ProductResponse> updateProductImage(@PathVariable UUID id, @PathVariable UUID imageId, @Valid @RequestBody ProductImageDto imageDto){
        return ResponseEntity.ok(productService.updateImage(id, imageId, imageDto));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ProductResponse> deleteProductImage(@PathVariable UUID id, @PathVariable UUID imageId){
        return ResponseEntity.ok(productService.deleteImage(id, imageId));
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariantDto>> getProductVariants(@PathVariable UUID id){
        return ResponseEntity.ok(productService.getProductVariants(id));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ProductResponse> addProductVariant(@PathVariable UUID id, @Valid @RequestBody ProductVariantDto variantDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addVariant(id, variantDto));
    }

    @PutMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ProductResponse> updateProductVariant(@PathVariable UUID id, @PathVariable UUID variantId, @Valid @RequestBody ProductVariantDto variantDto){
        return ResponseEntity.ok(productService.updateVariant(id, variantId, variantDto));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ProductResponse> deleteProductVariant(@PathVariable UUID id, @PathVariable UUID variantId){
        return ResponseEntity.ok(productService.deleteVariant(id, variantId));
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<ProductAttributeDto>> getProductAttributes(@PathVariable UUID id){
        return ResponseEntity.ok(productService.getProductAttributes(id));
    }

    @PostMapping("/{id}/attributes")
    public ResponseEntity<ProductResponse> addProductAttribute(@PathVariable UUID id, @Valid @RequestBody ProductAttributeDto attributeDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addAttribute(id, attributeDto));
    }

    @PutMapping("/{id}/attributes/{attributeId}")
    public ResponseEntity<ProductResponse> updateProductAttribute(@PathVariable UUID id, @PathVariable UUID attributeId, @Valid @RequestBody ProductAttributeDto attributeDto){
        return ResponseEntity.ok(productService.updateAttribute(id, attributeId, attributeDto));
    }

    @DeleteMapping("/{id}/attributes/{attributeId}")
    public ResponseEntity<ProductResponse> deleteProductAttribute(@PathVariable UUID id, @PathVariable UUID attributeId){
        return ResponseEntity.ok(productService.deleteAttribute(id, attributeId));
    }

    
}
