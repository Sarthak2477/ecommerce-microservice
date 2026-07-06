# Order Service

The **Order Service** coordinates order processing and lifecycle orchestration. It communicates synchronously with the Inventory Service and acts as an event producer to kick off asynchronous checkout sagas over Kafka.

---

## 1. Core Functions

* **Order Placement**: Receives checkout carts, validates pricing, and orchestrates stock reservations.
* **Sync Feign Client Integration**: Calls the `Inventory Service` synchronously to reserve stock before confirming an order.
* **Event Dispatching**: Emits `OrderPlacedEvent` messages to Kafka upon successful validation.
* **Saga Listener**: Listens for payment processing feedback (`PaymentCompletedEvent` / `PaymentFailedEvent`) to finalize or cancel/rollback the order.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Declarative Client**: Spring Cloud OpenFeign
* **Messaging**: Spring for Apache Kafka (Producer & Consumer)
* **Database**: PostgreSQL (`orderdb` database)

---

## 3. Port Configuration

* **Service Port**: `8082`
* **Kubernetes Hostname**: `http://order-service:8082`

---

## 4. Endpoint Registry

All endpoints are routed through the API Gateway and require a valid JWT bearer token.

### 1. Place a New Order
* **Method & URL**: `POST /api/orders`
* **Headers**: `Authorization: Bearer <TOKEN>`
* **Request Body**:
  ```json
  {
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "orderItems": [
      {
        "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
        "quantity": 2,
        "price": 49.99
      }
    ]
  }
  ```
* **Response Body (201 Created)**:
  ```json
  {
    "orderId": "3b2e5a61-12ef-45fa-a98b-21d3f92023fa",
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "totalPrice": 99.98,
    "status": "PENDING"
  }
  ```

### 2. Get Order Details By ID
* **Method & URL**: `GET /api/orders/{id}`

### 3. Get User Orders
* **Method & URL**: `GET /api/orders/user/{userId}`

### 4. Cancel Order
* **Method & URL**: `POST /api/orders/user/{userId}/cancel/{orderId}`
* **Note**: Immediately transitions order state to `CANCELLED` and makes a Feign call to Inventory Service to restore stock.

### 5. Update Order Status (Internal/Admin)
* **Method & URL**: `PATCH /api/orders/order/{id}/status?status=COMPLETED`

---

## 5. Feign Client Stock Reservation Flow

When `POST /api/orders` is executed:
1. The Order Service mapping interceptor extracts the authenticated user claims.
2. It constructs a request payload mapping variant IDs to quantities.
3. It makes a synchronous POST call via the Feign Client interface:
   `POST http://inventory-service:8083/api/inventory/reserve`
4. If the call fails or returns non-2xx status (e.g., out of stock), the order is rejected immediately with a `400 Bad Request`.
5. If successful, the database transaction is saved as `PENDING` and a message is pushed to Kafka.
