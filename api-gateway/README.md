# API Gateway Service

The **API Gateway** acts as the single point of entry for all external requests entering the microservices ecosystem. It handles intelligent path-based routing, load balancing, security verification, and rate limiting.

---

## 1. Core Functions

* **Reverse Proxy**: Proxies requests from clients to downstream microservices using direct internal Kubernetes DNS service hostnames.
* **Centralized JWT Authentication**: Intercepts incoming requests and validates JWT tokens using a custom Spring Cloud Gateway filter before forwarding them downstream.
* **Path-Based Routing**: Dynamically maps client-facing URIs (e.g., `/api/products/**`) to internal microservice service endpoints.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Gateway Library**: Spring Cloud Gateway (Netty/WebFlux-based non-blocking architecture)
* **Security**: JSON Web Tokens (JWT) using `io.jsonwebtoken`

---

## 3. Port Configuration & Routing Matrix

* **Service Port**: `8080`
* **Kubernetes Hostname**: `http://api-gateway:8080`

### Downstream Route Mappings:

| External Path | Target Internal URL | Authentication Required | Downstream Service |
| :--- | :--- | :--- | :--- |
| `/api/auth/**` | `http://user-service:8084` | No | `user-service` |
| `/api/users/**` | `http://user-service:8084` | Yes | `user-service` |
| `/api/products/**` | `http://product-service:8081` | Mixed (GET is public) | `product-service` |
| `/api/orders/**` | `http://order-service:8082` | Yes | `order-service` |
| `/api/inventory/**` | `http://inventory-service:8083` | Yes (Internal) | `inventory-service` |

---

## 4. JWT Authentication Filter Implementation

The gateway implements a custom **`JwtAuthenticationFilterFactory`** (`AbstractGatewayFilterFactory`).

1. **Header Inspection**: Looks for the `Authorization` header on every request mapping to protected resources.
2. **Token Extraction**: Extracts the `Bearer ` prefix to isolate the JWT string.
3. **Signature Verification**: Validates the signature and expiration time against the shared `JWT_SECRET` key.
4. **Header Enrichment**: Decrypts the token claims and adds the authenticated `userId` as an HTTP Header (`X-User-Id`) to enrich downstream requests.
5. **Rejection**: If the token is missing, expired, or tampered with, the gateway immediately returns `401 Unauthorized` without calling the downstream microservice.

---

## 5. Startup & Environment Variables

The gateway configuration is packaged locally inside the image as `api-gateway.properties` and uses the following overrides:

* `SPRING_CLOUD_CONFIG_ENABLED`: `false` (Standalone mode)
* `EUREKA_CLIENT_ENABLED`: `false` (Bypasses service registry; direct service routing)
* `JWT_SECRET`: Secret key used for cryptographic validation of the JWT signatures.
* `JAVA_TOOL_OPTIONS`: Heap bounds `-Xms32m -Xmx128m` to prevent OOM errors.
