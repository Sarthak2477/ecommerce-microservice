package com.example.payment_service.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.event.OrderPlacedEvent;
import com.example.payment_service.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    public Payment processPayment(OrderPlacedEvent event){
        PaymentStatus status;

        if(paymentRepository.findByOrderId(event.orderId()).isPresent()){
            log.info("Payment processed");
            throw new RuntimeException("Order Payment already processed.");
        }

        if(simulatePayment(event)) status = PaymentStatus.SUCCESS;
        else status = PaymentStatus.FAILED;

        Payment payment = Payment
                            .builder()
                            .transactionId(UUID.randomUUID())
                            .orderId(event.orderId())
                            .customerId(event.customerId())
                            .amount(event.totalPrice())
                            .createdAt(LocalDateTime.now())
                            .status(status)
                            .build();

        
        paymentRepository.save(payment);

        if(payment.getStatus() == PaymentStatus.SUCCESS)
            paymentProducer.publishSuccess(payment);
        else paymentProducer.publishFailure(payment);

        return payment;
    }

    private boolean simulatePayment(OrderPlacedEvent event) {

        if (event.orderId() == null)
            return false;

        if (event.customerId() == null)
            return false;

        if (event.totalPrice() == null)
            return false;

        return true;
    }
}
