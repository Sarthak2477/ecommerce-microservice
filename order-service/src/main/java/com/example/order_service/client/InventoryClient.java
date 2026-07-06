package com.example.order_service.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.order_service.dtos.ReduceStockRequest;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    @GetMapping("/check")
    boolean checkInventory(@RequestParam("variantId") UUID variantId, @RequestParam("quantity") Integer quantity);

    @PostMapping("/reduce")
    void reduceInventory(@RequestBody List<ReduceStockRequest> requestList);

    @PostMapping("/reserve")
    void reserveInventory(@RequestBody List<ReduceStockRequest> requestList);

    @PostMapping("/release")
    void releaseInventory(@RequestBody List<ReduceStockRequest> requestList);
}
