package com.example.payment_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.event.OrderPlacedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order-placed",
            groupId = "payment-group"
    )
    public void consume(OrderPlacedEvent event) {

        log.info("Recieved Order: ");
        log.info(event.toString());

        Payment payment = paymentService.processPayment(event);
        log.info("Payment Status : " + payment.getStatus());
    }
    
}
