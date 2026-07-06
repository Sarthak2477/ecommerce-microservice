package com.example.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.payment_service.entity.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>{
    List<Payment> findByCustomerId(UUID customerId);
    Optional<Payment> findByTransactionId(UUID transactionId);
    Optional<Payment> findByOrderId(UUID orderId);
}
