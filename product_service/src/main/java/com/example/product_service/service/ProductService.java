package com.example.product_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.product_service.dto.*;
import com.example.product_service.entity.*;
import com.example.product_service.errors.DuplicateResourceException;
import com.example.product_service.errors.ResourceNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getProducts(){
        return productRepository.findAll().stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductResponse> getProduct(@NonNull UUID id){
        return productRepository.findById(id)
                .map(ProductMapper::toDto);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest productRequest){
        if (productRepository.existsBySku(productRequest.getSku())) {
            throw new DuplicateResourceException("Product with SKU " + productRequest.getSku() + " already exists");
        }
        if (productRepository.existsBySlug(productRequest.getSlug())) {
            throw new DuplicateResourceException("Product with slug " + productRequest.getSlug() + " already exists");
        }
        Product product = Product.builder()
                .sku(productRequest.getSku())
                .slug(productRequest.getSlug())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .shortDescription(productRequest.getShortDescription())
                .categoryId(productRequest.getCategoryId())
                .brandId(productRequest.getBrandId())
                .status("ACTIVE") 
                .build();
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest productRequest){
        Product product = productRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("Product with ID" + "not found."));

        if (productRequest.getSku() != null && !productRequest.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(productRequest.getSku())) {
                throw new DuplicateResourceException("Product with SKU " + productRequest.getSku() + " already exists");
            }
            product.setSku(productRequest.getSku());
        }

        if (productRequest.getSlug() != null && !productRequest.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlug(productRequest.getSlug())) {
                throw new DuplicateResourceException("Product with slug " + productRequest.getSlug() + " already exists");
            }
            product.setSlug(productRequest.getSlug());
        }
        if (productRequest.getName() != null) product.setName(productRequest.getName());
        if (productRequest.getDescription() != null) product.setDescription(productRequest.getDescription());
        if (productRequest.getShortDescription() != null) product.setShortDescription(productRequest.getShortDescription());
        if (productRequest.getCategoryId() != null) product.setCategoryId(productRequest.getCategoryId());
        if (productRequest.getBrandId() != null) product.setBrandId(productRequest.getBrandId());

        Product updatedProduct = productRepository.save(product);
        return ProductMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id){
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);

    }

    @Transactional
    public List<ProductImageDto> getImages(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));
        return product.getImages().stream()
                .map(ProductMapper::toImageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse addImage(UUID productId, ProductImageDto imageDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        if (product.getImages() == null) {
            product.setImages(new java.util.ArrayList<>());
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageDto.getImageUrl())
                .isPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false)
                .sortOrder(imageDto.getSortOrder() != null ? imageDto.getSortOrder() : 0)
                .build();

        product.getImages().add(image);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateImage(UUID productId, UUID imageId, ProductImageDto imageDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        ProductImage image = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image with ID " + imageId + " not found on product " + productId));

        if (imageDto.getImageUrl() != null) image.setImageUrl(imageDto.getImageUrl());
        if (imageDto.getIsPrimary() != null) image.setIsPrimary(imageDto.getIsPrimary());
        if (imageDto.getSortOrder() != null) image.setSortOrder(imageDto.getSortOrder());

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse deleteImage(UUID productId, UUID imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        boolean removed = product.getImages().removeIf(img -> img.getId().equals(imageId));
        if (!removed) {
            throw new ResourceNotFoundException("Image with ID " + imageId + " not found on product " + productId);
        }

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    // --- Product Variant CRUD ---

    @Transactional
    public List<ProductVariantDto> getProductVariants(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));
        return product.getVariants().stream()
                .map(ProductMapper::toVariantDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public ProductResponse addVariant(UUID productId, ProductVariantDto variantDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        if (productRepository.existsByVariantSku(variantDto.getSku())) {
            throw new DuplicateResourceException("Variant with SKU " + variantDto.getSku() + " already exists");
        }

        if (product.getVariants() == null) {
            product.setVariants(new java.util.ArrayList<>());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantDto.getSku())
                .variantName(variantDto.getVariantName())
                .color(variantDto.getColor())
                .size(variantDto.getSize())
                .price(variantDto.getPrice())
                .weight(variantDto.getWeight())
                .build();

        product.getVariants().add(variant);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateVariant(UUID productId, UUID variantId, ProductVariantDto variantDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Variant with ID " + variantId + " not found on product " + productId));

        if (variantDto.getSku() != null && !variantDto.getSku().equals(variant.getSku())) {
            if (productRepository.existsByVariantSku(variantDto.getSku())) {
                throw new DuplicateResourceException("Variant with SKU " + variantDto.getSku() + " already exists");
            }
            variant.setSku(variantDto.getSku());
        }

        if (variantDto.getVariantName() != null) variant.setVariantName(variantDto.getVariantName());
        if (variantDto.getColor() != null) variant.setColor(variantDto.getColor());
        if (variantDto.getSize() != null) variant.setSize(variantDto.getSize());
        if (variantDto.getPrice() != null) variant.setPrice(variantDto.getPrice());
        if (variantDto.getWeight() != null) variant.setWeight(variantDto.getWeight());

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse deleteVariant(UUID productId, UUID variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        boolean removed = product.getVariants().removeIf(v -> v.getId().equals(variantId));
        if (!removed) {
            throw new ResourceNotFoundException("Variant with ID " + variantId + " not found on product " + productId);
        }

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    // --- Product Attribute CRUD ---

    @Transactional
    public List<ProductAttributeDto> getProductAttributes(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));
        return product.getAttributes().stream()
                .map(ProductMapper::toAttributeDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public ProductResponse addAttribute(UUID productId, ProductAttributeDto attributeDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        if (product.getAttributes() == null) {
            product.setAttributes(new java.util.ArrayList<>());
        }

        ProductAttribute attribute = ProductAttribute.builder()
                .product(product)
                .attributeName(attributeDto.getAttributeName())
                .attributeValue(attributeDto.getAttributeValue())
                .build();

        product.getAttributes().add(attribute);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse updateAttribute(UUID productId, UUID attributeId, ProductAttributeDto attributeDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        ProductAttribute attribute = product.getAttributes().stream()
                .filter(attr -> attr.getId().equals(attributeId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Attribute with ID " + attributeId + " not found on product " + productId));

        if (attributeDto.getAttributeName() != null) attribute.setAttributeName(attributeDto.getAttributeName());
        if (attributeDto.getAttributeValue() != null) attribute.setAttributeValue(attributeDto.getAttributeValue());

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductResponse deleteAttribute(UUID productId, UUID attributeId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found"));

        boolean removed = product.getAttributes().removeIf(attr -> attr.getId().equals(attributeId));
        if (!removed) {
            throw new ResourceNotFoundException("Attribute with ID " + attributeId + " not found on product " + productId);
        }

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toDto(savedProduct);
    }
}
