# Product Service

The **Product Service** manages the core inventory catalog, containing product details, multi-image mappings, attributes, and variant configurations.

---

## 1. Core Functions

* **Product Catalog**: Full CRUD operations for products.
* **Product Images**: Multi-image attachments per product for frontend gallery loading.
* **Product Variants**: Size, color, and option splits (e.g. Medium/Black vs Large/Red) mapped to unique stock IDs.
* **Product Attributes**: Custom metadata key-value pairs (e.g., "Material": "Cotton").
* **Redis Caching**: Caches frequent product catalog read queries to reduce database read load and latency.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Data Access**: Spring Data JPA, Hibernate, PostgreSQL Driver
* **Caching**: Spring Boot Starter Data Redis
* **Database**: PostgreSQL (`productdb` database)

---

## 3. Port Configuration

* **Service Port**: `8081`
* **Kubernetes Hostname**: `http://product-service:8081`

---

## 4. Endpoint Registry

All endpoints are routed through the API Gateway.

### A. Product CRUD Endpoints (`/api/products`)

#### 1. List All Products
* **Method & URL**: `GET /api/products`
* **Response Body (200 OK)**:
  ```json
  [
    {
      "id": "e0e85a62-fc8e-4a6f-a65c-6b3a322c36df",
      "name": "Classic Crewneck",
      "description": "Premium organic cotton crewneck sweatshirt",
      "price": 49.99
    }
  ]
  ```

#### 2. Get Product By ID
* **Method & URL**: `GET /api/products/{id}`

#### 3. Create Product
* **Method & URL**: `POST /api/products`
* **Headers**: `Authorization: Bearer <TOKEN>`
* **Request Body**:
  ```json
  {
    "name": "Classic Crewneck",
    "description": "Premium organic cotton crewneck sweatshirt",
    "price": 49.99
  }
  ```

#### 4. Update Product
* **Method & URL**: `PUT /api/products/{id}`

#### 5. Delete Product
* **Method & URL**: `DELETE /api/products/{id}`

---

### B. Image Mappings (`/api/products/{id}/images`)

* `GET /api/products/{id}/images`: Fetch images.
* `POST /api/products/{id}/images`: Add image.
* `PUT /api/products/{id}/images/{imageId}`: Update image details.
* `DELETE /api/products/{id}/images/{imageId}`: Remove image.

---

### C. Variant Configurations (`/api/products/{id}/variants`)

* `GET /api/products/{id}/variants`: Fetch variants.
* `POST /api/products/{id}/variants`: Add variant (e.g. Size: M, SKU: CREW-SWEAT-M).
* `PUT /api/products/{id}/variants/{variantId}`: Update variant details.
* `DELETE /api/products/{id}/variants/{variantId}`: Remove variant.

---

### D. Attribute Specifications (`/api/products/{id}/attributes`)

* `GET /api/products/{id}/attributes`: Fetch attributes.
* `POST /api/products/{id}/attributes`: Add custom attribute key/value.
* `PUT /api/products/{id}/attributes/{attributeId}`: Update attribute.
* `DELETE /api/products/{id}/attributes/{attributeId}`: Delete attribute.

---

## 5. Caching Strategy with Redis

To optimize latency, the Product Service integrates Redis caching:
* Read requests (`GET /api/products` and `GET /api/products/{id}`) check the Redis cache first.
* If a cache hit occurs, it returns the cached result immediately, bypassing PostgreSQL.
* If a cache miss occurs, the data is fetched from PostgreSQL and written to Redis.
* Write operations (`POST`, `PUT`, `DELETE` on products, variants, images, or attributes) automatically evict related cache keys to guarantee data consistency.
