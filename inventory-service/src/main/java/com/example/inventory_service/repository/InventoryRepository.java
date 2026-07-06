package com.example.inventory_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inventory_service.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID>{
    Optional<Inventory> findByVariantId(UUID variantId);
    List<Inventory> findByVariantIdIn(List<UUID> variantIds);   
}
