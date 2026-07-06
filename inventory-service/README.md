# Inventory Service

The **Inventory Service** manages physical stock allocations, tracking availability at the variant level. It exposes endpoints designed for consumption by other microservices (such as the Order Service).

---

## 1. Core Functions

* **Stock Management**: Restores or adds stock when new shipments are logged.
* **Synchronous Reservation**: Checks variant availability and isolates/reserves items during checkout to prevent double-selling.
* **Synchronous Release**: Restores reserved stock immediately if a checkout fails or is cancelled.
* **Stock Deductions**: Permanently decrements reserved quantities upon successful payment verification.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Data Access**: Spring Data JPA, Hibernate, PostgreSQL Driver
* **Database**: PostgreSQL (`inventorydb` database)

---

## 3. Port Configuration

* **Service Port**: `8083`
* **Kubernetes Hostname**: `http://inventory-service:8083`

---

## 4. Endpoint Registry

All reservation and stock update endpoints are consumed internally via Feign Clients or direct service routing.

### 1. Add Stock / Create Inventory Card
* **Method & URL**: `POST /api/inventory`
* **Request Body**:
  ```json
  {
    "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
    "quantity": 100
  }
  ```
* **Response Body (201 Created)**:
  ```json
  {
    "id": "7b2e5a61-12ef-45fa-a98b-21d3f92023fa",
    "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
    "quantity": 100
  }
  ```

### 2. Check Stock Availability
* **Method & URL**: `GET /api/inventory/check?variantId=e0e85a62-fc8e-4a6f-a65c-6b3a322c36df&quantity=2`
* **Response**: `true` or `false`

### 3. Reserve Stock (During Checkout)
* **Method & URL**: `POST /api/inventory/reserve`
* **Request Body**:
  ```json
  [
    {
      "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
      "quantity": 2
    }
  ]
  ```
* **Response**: `200 OK`

### 4. Release Reserved Stock (Saga Rollback / Order Cancelled)
* **Method & URL**: `POST /api/inventory/release`
* **Request Body**:
  ```json
  [
    {
      "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
      "quantity": 2
    }
  ]
  ```
* **Response**: `200 OK`

### 5. Deduct Stock Permanently (Order Completed)
* **Method & URL**: `POST /api/inventory/reduce`
* **Request Body**:
  ```json
  [
    {
      "variantId": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
      "quantity": 2
    }
  ]
  ```
* **Response**: `200 OK`
