package com.example.payment_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.event.PaymentFailedEvent;
import com.example.payment_service.event.PaymentSuccessEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSuccess(Payment payment) {

        PaymentSuccessEvent event = new PaymentSuccessEvent(
                payment.getOrderId(),
                payment.getTransactionId(),
                payment.getCustomerId(),
                payment.getAmount()
        );

        kafkaTemplate.send("payment-success", event);
    }

    public void publishFailure(Payment payment) {

        PaymentFailedEvent event = new PaymentFailedEvent(
                payment.getOrderId(),
                "Payment validation failed"
        );

        kafkaTemplate.send("payment-failed", event);
    }
}
