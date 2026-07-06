package com.example.payment_service.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSuccessEvent(
    UUID orderId,
    UUID transactionId,
    UUID customerId,
    BigDecimal amount
) {

    

}