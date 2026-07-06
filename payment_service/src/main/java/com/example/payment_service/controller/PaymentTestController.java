package com.example.payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.event.OrderPlacedEvent;
import com.example.payment_service.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments/test")
@RequiredArgsConstructor
public class PaymentTestController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> testProcessPayment(@RequestBody OrderPlacedEvent event) {
        Payment payment = paymentService.processPayment(event);
        return ResponseEntity.ok(payment);
    }
}
