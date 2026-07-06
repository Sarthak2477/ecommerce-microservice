package com.example.order_service.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.order_service.entity.OrderStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
    
    private final OrderService orderService;
    
    record PaymentSuccessEvent(UUID orderId,UUID transactionId,UUID customerId,BigDecimal amount) {}
    record PaymentFailedEvent(UUID orderId,String reason) {}
    
     @KafkaListener(
            topics = "payment-success",
            groupId = "payment-group"
    )
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Order Id: " + event.orderId() + "Payment Paid.");
        orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);
    }

    @KafkaListener(
        topics = "payment-failed",
        groupId = "payment-group"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Order Id: " + event.orderId() + "Payment Failed.");
        orderService.updateOrderStatus(event.orderId(), OrderStatus.PAYMENT_FAILED);
    }
}
