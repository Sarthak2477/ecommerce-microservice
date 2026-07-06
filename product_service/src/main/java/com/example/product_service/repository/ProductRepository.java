package com.example.product_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductImage;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>{
    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    List<Product> findByStatus(String status);

    List<Product> findByCategoryId(UUID categoryId);

    List<Product> findByBrandId(UUID brandId);

    @Query("SELECT COUNT(v) > 0 FROM Product p JOIN p.variants v WHERE v.sku = :sku")
    boolean existsByVariantSku(String sku);

}
