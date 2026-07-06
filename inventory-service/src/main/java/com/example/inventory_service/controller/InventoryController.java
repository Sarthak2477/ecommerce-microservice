package com.example.inventory_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_service.dto.AddInventoryRequest;
import com.example.inventory_service.dto.InventoryResponse;
import com.example.inventory_service.dto.ReduceStockRequest;
import com.example.inventory_service.dto.ReserveStockRequest;
import com.example.inventory_service.dto.ReleaseStockRequest;
import com.example.inventory_service.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;

    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@Valid @RequestBody @NonNull AddInventoryRequest addInventoryRequest){
        InventoryResponse inventoryResponse = service.addInventory(addInventoryRequest);
        return ResponseEntity.status((HttpStatus.CREATED))
                .body(inventoryResponse);
    }

    @DeleteMapping("/id")
    public ResponseEntity<Void> deleteInventory(@RequestParam UUID variantId){
        service.deleteInventory(variantId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkInventory(@RequestParam UUID variantId, @RequestParam Integer quantity){
        return ResponseEntity.ok(service.checkInventory(variantId, quantity));
    }

    @PostMapping("/reduce")
    public ResponseEntity<Void> reduceInventory(@RequestBody List<ReduceStockRequest> requestList){
        service.reduceInventory(requestList);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveInventory(@RequestBody List<ReserveStockRequest> requestList){
        service.reserveInventory(requestList);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/release")
    public ResponseEntity<Void> releaseInventory(@RequestBody List<ReleaseStockRequest> requestList){
        service.releaseInventory(requestList);
        return ResponseEntity.ok().build();
    }
}
