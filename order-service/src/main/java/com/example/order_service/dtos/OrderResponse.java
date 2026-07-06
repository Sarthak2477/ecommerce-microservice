package com.example.order_service.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
    UUID orderid,
    String status,
    BigDecimal totalAmount
) {
   

}
