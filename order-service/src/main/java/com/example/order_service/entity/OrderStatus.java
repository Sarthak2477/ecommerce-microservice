package com.example.order_service.entity;

/**
 * OrderStatus
 */
public enum OrderStatus {
    PENDING,

    INVENTORY_RESERVED,

    PAYMENT_PENDING,

    PAID,

    PAYMENT_FAILED,

    CANCELLED,

    SHIPPED,

    DELIVERED
}
