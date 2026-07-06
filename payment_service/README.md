# Payment Service

The **Payment Service** processes order transactions asynchronously. It reacts to customer checkout events, simulates/processes the payment gateways, and publishes completion status events back to Kafka.

---

## 1. Core Functions

* **Asynchronous Listening**: Consumes `OrderPlacedEvent` from the `order-placed` Kafka topic.
* **Transaction Processing**: Records transaction attempts in PostgreSQL and validates payment credentials.
* **State Propagation**: Publishes `PaymentCompletedEvent` or `PaymentFailedEvent` to Kafka depending on the result, allowing the Order Service to finalize or roll back.
* **Test Interface**: Exposes a manual testing REST endpoint to submit simulated checkout payloads.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Messaging**: Spring for Apache Kafka (Consumer & Producer)
* **Data Access**: Spring Data JPA, Hibernate, PostgreSQL Driver
* **Database**: PostgreSQL (`paymentdb` database)

---

## 3. Port Configuration

* **Service Port**: `8086`
* **Kubernetes Hostname**: `http://payment-service:8086`

---

## 4. Endpoint Registry

This service primarily operates as an event listener, but provides a test endpoint for sandbox validation:

### 1. Test Process Payment (Simulated Event Input)
* **Method & URL**: `POST /api/payments/test`
* **Request Body**:
  ```json
  {
    "orderId": "3b2e5a61-12ef-45fa-a98b-21d3f92023fa",
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "totalPrice": 99.98,
    "orderItems": [
      {
        "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
        "quantity": 2
      }
    ]
  }
  ```
* **Response Body (200 OK)**:
  ```json
  {
    "id": "e4f8a61d-8472-4b2a-89cf-12e0fba75620",
    "orderId": "3b2e5a61-12ef-45fa-a98b-21d3f92023fa",
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "amount": 99.98,
    "paymentStatus": "COMPLETED",
    "transactionId": "TXN-908124976"
  }
  ```

---

## 5. Event Loop Choreography

1. **Consumer**: Subscribes to topic `order-placed`.
2. **Action**:
   * Reads incoming message.
   * Maps properties to local POJOs.
   * Simulates payment gateway processing (e.g. Stripe, PayPal, or automated sandbox approval).
   * Records details in PostgreSQL table `payments` with a unique transaction ID.
3. **Producer**:
   * If payment succeeds: Publishes `PaymentCompletedEvent` (contains `orderId`, `transactionId`) to topic `payment-completed`.
   * If payment fails: Publishes `PaymentFailedEvent` (contains `orderId`, error details) to topic `payment-failed`.
