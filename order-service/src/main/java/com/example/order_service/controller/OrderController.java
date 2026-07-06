package com.example.order_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_service.dtos.CreateOrderRequest;
import com.example.order_service.dtos.OrderResponse;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.service.OrderService;

import io.github.resilience4j.core.lang.NonNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@NonNull @PathVariable("id") UUID orderId){
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrderByUserId(@NonNull @PathVariable("userId") UUID userId){
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    } 

    @PostMapping("/user/{userId}/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@NonNull @PathVariable("userId") UUID userId, @NonNull @PathVariable("orderId") UUID orderId){
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/order/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable("id") UUID id, 
            @RequestParam("status") OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.noContent().build();
    }

}