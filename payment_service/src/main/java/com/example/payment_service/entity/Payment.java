package com.example.payment_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import io.github.resilience4j.core.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private UUID orderId;

    private UUID customerId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS, FAILED
    @NonNull
    private UUID transactionId;

    @DateTimeFormat
    private LocalDateTime createdAt;
}