package com.example.payment_service.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
    UUID orderId,
    UUID customerId,
    BigDecimal totalPrice
) {
}
