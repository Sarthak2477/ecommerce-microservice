package com.example.notification_service.service;


import com.example.notification_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "order-placed", groupId = "notification-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info(
            "Notification sent for Order ID: {} to Customer: {}",
            event.orderId(),
            event.customerId()
        );
    }
}
