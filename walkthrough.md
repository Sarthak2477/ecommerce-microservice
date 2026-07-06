# E-commerce Microservices End-to-End Testing Guide

This guide details how to verify the functionality of the entire microservices ecosystem. It covers registration, service discovery, JWT authorization, inventory checks, order orchestration, payment completion, and Kafka event propagation.

---

## 1. Prerequisites & Port Mappings
Ensure all containers are running and healthy. You can check them using `docker ps`. The key entry points are:

| Service / Tool | External Port | URL |
| :--- | :--- | :--- |
| **API Gateway** | `8080` | `http://localhost:8080` |
| **Eureka Discovery Server** | `8761` | `http://localhost:8761` |
| **Kafdrop (Kafka UI)** | `9000` | `http://localhost:9000` |
| **Config Server** | `8888` | `http://localhost:8888` |

---

## 2. Infrastructure Health Checks

### Check Eureka Registry
Open [http://localhost:8761](http://localhost:8761) in your browser. You should see all application services registered:
* `API-GATEWAY`
* `USER-SERVICE`
* `PRODUCT-SERVICE`
* `ORDER-SERVICE`
* `INVENTORY-SERVICE`
* `PAYMENT-SERVICE`
* `NOTIFICATION-SERVICE`

### Check Kafka UI (Kafdrop)
Open [http://localhost:9000](http://localhost:9000) in your browser to verify that Kafka is running and all required event topics (e.g., `order-placed`, `payment-processed`) exist.

---

## 3. End-to-End Testing Walkthrough (via API Gateway)

We will use the API Gateway on port `8080` to route all microservice requests. You can import `Payment_Service_Postman_Collection.json` into Postman to perform these steps.

### Step 1: Register User
Create a new user account in `user-service`.

* **HTTP Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/register`
* **Headers**: `Content-Type: application/json`
* **Request Body**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```
* **Expected Response**: `201 Created` or a success message containing the user entity.

---

### Step 2: Login User (Get JWT Token)
Authenticate to obtain the Bearer token needed for downstream services.

* **HTTP Method**: `POST`
* **URL**: `http://localhost:8080/api/auth/login`
* **Headers**: `Content-Type: application/json`
* **Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```
* **Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
> [!IMPORTANT]
> Copy the returned `token` value. You will need it for the next requests.

---

### Step 3: Place Order
Place an order using a pre-seeded variant ID that has available stock in the inventory database.

* **HTTP Method**: `POST`
* **URL**: `http://localhost:8080/api/orders`
* **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <YOUR_JWT_TOKEN>`
* **Request Body**:
```json
{
  "items": [
    {
      "id": "47f7d540-df51-4171-be98-634a6a575a7c",
      "quantity": 1,
      "price": 250.00,
      "subtotal": 250.00
    }
  ]
}
```
> [!NOTE]
> The product item `id` corresponds to the `variant_id` in the `inventory-service` database. We are using `47f7d540-df51-4171-be98-634a6a575a7c` which is seeded with `100` items in `inventory_db.sql`.

* **Expected Response**: `201 Created` or `200 OK` with the created order details and status `PENDING`. Copy the returned `id` (Order ID).

---

### Step 4: Verify Asynchronous Order Completion (Saga Flow)
Once the order is placed:
1. `order-service` publishes an `OrderPlacedEvent` to Kafka.
2. `payment-service` consumes it, processes the payment (auto-approves in test mode), and publishes a `PaymentProcessedEvent`.
3. `order-service` consumes the payment status and marks the order as `PAID` or `CONFIRMED`.
4. `notification-service` logs a message indicating that the order status was sent to the user.

To verify this, query the order status:

* **HTTP Method**: `GET`
* **URL**: `http://localhost:8080/api/orders/<ORDER_ID>`
* **Headers**:
  * `Authorization: Bearer <YOUR_JWT_TOKEN>`
* **Expected Response**:
  * The order status should now be updated to **`CONFIRMED`** or **`PAID`** (depending on your service implementation).

---

## 4. Troubleshooting Logs
If any step fails, you can inspect individual service logs via Docker:
```powershell
# See logs for all services:
docker-compose logs -f

# See logs for a specific service:
docker-compose logs -f payment-service
docker-compose logs -f order-service
```
