package com.example.payment_service.event;

import java.util.UUID;

public record PaymentFailedEvent(
    UUID orderId,
    String reason
) {
    
}
