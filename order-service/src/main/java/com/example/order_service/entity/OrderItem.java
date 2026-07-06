package com.example.order_service.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "order_items")
@Getter
@Setter
@RequiredArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID productId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;

    @ManyToOne
    private Order order;
}
