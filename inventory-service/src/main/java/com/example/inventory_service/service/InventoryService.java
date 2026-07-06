package com.example.inventory_service.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.dto.AddInventoryRequest;
import com.example.inventory_service.dto.InventoryResponse;
import com.example.inventory_service.dto.ReduceStockRequest;
import com.example.inventory_service.dto.ReserveStockRequest;
import com.example.inventory_service.dto.ReleaseStockRequest;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.mapper.InventoryMapper;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Getter
@Setter
public class InventoryService {
    private final InventoryRepository productrepo;
    private final InventoryMapper mapper;

    @Transactional
    public InventoryResponse addInventory(@NonNull AddInventoryRequest addInventoryRequest) {
        Optional<Inventory> existingInventory = productrepo.findByVariantId(addInventoryRequest.variantId());

        Inventory inventory;

        if (existingInventory.isPresent()) {
            // Restock existing inventory
            inventory = existingInventory.get();
            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + addInventoryRequest.availableQuantity());

            // Update other fields if needed
            inventory.setWarehouseLocation(addInventoryRequest.warehouseLocation());

        } else {
            inventory = mapper.toEntity(addInventoryRequest);
        }

        productrepo.save(inventory);

        return mapper.toResponse(inventory);
    }

    public boolean checkInventory(UUID variantID, Integer quantity) {
        return productrepo.findByVariantId(variantID)
                .map(inventory -> {
                    int availableStock = inventory.getAvailableQuantity() - inventory.getReservedQuantity();
                    return availableStock >= quantity;
                })
                .orElse(false);
    }

    @Transactional
    public void deleteInventory(UUID variantId) {
        Inventory inventory = productrepo.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        productrepo.delete(inventory);
    }

    @Transactional
    public void reduceInventory(List<ReduceStockRequest> requestList) {

        // Extract all variantIds
        Set<UUID> variantIds = requestList.stream()
                .map(ReduceStockRequest::variantId)
                .collect(Collectors.toSet());

        List<Inventory> inventories = productrepo.findByVariantIdIn(new ArrayList<>(variantIds));

        Map<UUID, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getVariantId,
                        Function.identity()));

        for (ReduceStockRequest request : requestList) {

            Inventory inventory = inventoryMap.get(request.variantId());

            if (inventory == null) {
                throw new RuntimeException(
                        "Inventory not found for variant: " + request.variantId());
            }

            int available = inventory.getAvailableQuantity()
                    - inventory.getReservedQuantity();

            if (available < request.quantity()) {
                throw new RuntimeException(
                        "Insufficient stock for variant: " + request.variantId());
            }

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - request.quantity());
        }

        productrepo.saveAll(inventories);
    }

    @Transactional
    public void reserveInventory(List<ReserveStockRequest> requestList) {
        // Extract all variantIds
        Set<UUID> variantIds = requestList.stream()
                .map(ReserveStockRequest::variantId)
                .collect(Collectors.toSet());

        // Fetch all inventories in one query
        List<Inventory> inventories = productrepo.findByVariantIdIn(new ArrayList<>(variantIds));

        Map<UUID, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getVariantId,
                        Function.identity()));

        for (ReserveStockRequest request : requestList) {

            Inventory inventory = inventoryMap.get(request.variantId());

            if (inventory == null) {
                throw new RuntimeException("Inventory not found for variant: " + request.variantId());
            }

            int available = inventory.getAvailableQuantity() - inventory.getReservedQuantity();

            if (available < request.quantity()) {
                throw new RuntimeException("Insufficient stock for variant: " + request.variantId());
            }

            inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        }

        productrepo.saveAll(inventories);
    }

    @Transactional
    public void releaseInventory(List<ReleaseStockRequest> requestList) {
        // Extract all variantIds
        Set<UUID> variantIds = requestList.stream()
                .map(ReleaseStockRequest::variantId)
                .collect(Collectors.toSet());

        // Fetch all inventories in one query
        List<Inventory> inventories = productrepo.findByVariantIdIn(new ArrayList<>(variantIds));

        Map<UUID, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getVariantId,
                        Function.identity()));

        for (ReleaseStockRequest request : requestList) {

            Inventory inventory = inventoryMap.get(request.variantId());

            if (inventory == null) {
                throw new RuntimeException("Inventory not found for variant: " + request.variantId());
            }

            int reserved = inventory.getReservedQuantity();

            if (reserved < request.quantity()) {
                throw new RuntimeException("No reserved stock to release for variant: " + request.variantId());
            }

            inventory.setReservedQuantity(reserved - request.quantity());
            
            // Add back to available quantity
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.quantity());
        }

        productrepo.saveAll(inventories);
    }
}
