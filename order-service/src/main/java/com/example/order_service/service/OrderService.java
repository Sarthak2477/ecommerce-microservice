package com.example.order_service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.dtos.CreateOrderRequest;
import com.example.order_service.dtos.OrderItemRequest;
import com.example.order_service.dtos.OrderResponse;
import com.example.order_service.dtos.ReduceStockRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.event.OrderPlacedEvent;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final HttpServletRequest httpServletRequest;

    public OrderService(OrderRepository orderRepository, 
                        OrderMapper orderMapper, 
                        InventoryClient inventoryClient,
                        KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
                        HttpServletRequest httpServletRequest){
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
        this.httpServletRequest = httpServletRequest;
    }

    @Transactional
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "placeOrderFallback")
    public OrderResponse createOrder(CreateOrderRequest request){
        log.info("Starting order placement process");

        // 1. Fetch user context from HTTP Headers passed by API Gateway
        String userIdHeader = httpServletRequest.getHeader("X-User-Id");
        UUID customerId = null;
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                customerId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid X-User-Id format: {}", userIdHeader);
            }
        }

        // 2. Map request to Order entity and set initial state
        Order order = orderMapper.toEntity(request);
        order.setUserId(customerId);
        order.setStatus(OrderStatus.PENDING);

        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }

        // Calculate total amount
        BigDecimal total = order.getItems() != null ? 
            order.getItems().stream()
                .map(item -> item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;
        order.setTotalAmount(total);

        // 3. Save the order to DB as PENDING
        Order savedOrder = orderRepository.save(order);
        log.info("Order created in PENDING state with ID: {}", savedOrder.getId());

        // 4. Trigger stock reservation via Feign (internally validates availability)
        if (request.items() != null && !request.items().isEmpty()) {
            List<ReduceStockRequest> reserveRequests = request.items().stream()
                .map(item -> new ReduceStockRequest(item.id(), item.quantity()))
                .toList();
            inventoryClient.reserveInventory(reserveRequests);
        }

        // 5. Update order status to indicate inventory is reserved
        savedOrder.setStatus(OrderStatus.INVENTORY_RESERVED);
        savedOrder = orderRepository.save(savedOrder);
        log.info("Inventory reserved successfully for Order ID: {}", savedOrder.getId());

        // 6. Publish OrderPlacedEvent to Kafka topic 'order-placed'
        OrderPlacedEvent event = new OrderPlacedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());
        try {
            kafkaTemplate.send("order-placed", event);
            log.info("Published OrderPlacedEvent to Kafka for Order ID: {}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to send OrderPlacedEvent to Kafka for Order ID: {}", savedOrder.getId(), e);
        }

        return orderMapper.toResponse(savedOrder);
    }

    public OrderResponse getOrder(UUID orderId){
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(()-> new RuntimeException("Unable to find the Order ID:  " + orderId ));

        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getOrdersByUser(UUID userId){
        List<Order> orders = orderRepository.findByUserId(userId);
        
        return orders.stream()
                    .map(orderMapper::toResponse)
                    .toList();
    }

    public void updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(()-> new RuntimeException("Unable to find the Order ID:  " + orderId ));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        orderRepository.save(order);

        // If transitioning to CANCELLED from a reserved state, release the inventory
        if (status == OrderStatus.CANCELLED && oldStatus == OrderStatus.INVENTORY_RESERVED) {
            releaseOrderInventory(order);
        }
    }

    public void cancelOrder(UUID orderId, UUID userId){
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(()-> new RuntimeException("Unable to find the Order ID:  " + orderId ));

        if(!order.getUserId().equals(userId)){
            throw new RuntimeException("Unauthorized to cancel the Order ID:  " + orderId );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Release inventory if it was reserved
        if (oldStatus == OrderStatus.INVENTORY_RESERVED) {
            releaseOrderInventory(order);
        }
    }

    private void releaseOrderInventory(Order order) {
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<ReduceStockRequest> releaseRequests = order.getItems().stream()
                .map(item -> new ReduceStockRequest(item.getProductId(), item.getQuantity()))
                .toList();
            try {
                inventoryClient.releaseInventory(releaseRequests);
                log.info("Released inventory for Order ID: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to release inventory for Order ID: {}", order.getId(), e);
                throw new RuntimeException("Could not release inventory: " + e.getMessage(), e);
            }
        }
    }

    public OrderResponse placeOrderFallback(CreateOrderRequest request, Throwable throwable) {
        log.error("Fallback triggered due to: {}", throwable.getMessage(), throwable);
        throw new RuntimeException("Order service is currently unable to reach the Inventory service. Please try again later.");
    }

    

}
