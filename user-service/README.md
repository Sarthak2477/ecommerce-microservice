# User Service

The **User Service** manages user accounts, user profiles, credentials, and authenticates API consumers by generating secure JSON Web Tokens (JWT).

---

## 1. Core Functions

* **User Registration**: Enrolls new customers and encrypts passwords before database persistence.
* **Authentication & Login**: Validates credentials and generates signed JWTs containing user claims (id, role, username).
* **Profile Resolution**: Provides REST endpoints for fetching user details by ID for downstream services.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Security & Crypto**: Spring Security (BCrypt Password Encoder), `io.jsonwebtoken`
* **Data Access**: Spring Data JPA, Hibernate, PostgreSQL Driver
* **Database**: PostgreSQL (`userdb` database)

---

## 3. Port Configuration

* **Service Port**: `8084`
* **Kubernetes Hostname**: `http://user-service:8084`

---

## 4. Endpoint Registry

All endpoints are prefix-routed through the API Gateway.

### A. Authentication Endpoints (`/api/auth`)

#### 1. Register User
* **Method & URL**: `POST /api/auth/register`
* **Request Body**:
  ```json
  {
    "username": "sarthak",
    "password": "mySecurePassword123",
    "email": "sarthak@example.com"
  }
  ```
* **Response Body (200 OK / 201 Created)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "username": "sarthak"
  }
  ```

#### 2. User Login
* **Method & URL**: `POST /api/auth/login`
* **Request Body**:
  ```json
  {
    "username": "sarthak",
    "password": "mySecurePassword123"
  }
  ```
* **Response Body (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "username": "sarthak"
  }
  ```

---

### B. User Endpoints (`/api/users`)

#### 1. Get User Profile by ID
* **Method & URL**: `GET /api/users/{id}`
* **Headers**: `Authorization: Bearer <TOKEN>`
* **Response Body (200 OK)**:
  ```json
  {
    "id": "d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b",
    "username": "sarthak",
    "email": "sarthak@example.com",
    "roles": ["ROLE_USER"]
  }
  ```

---

## 5. Security & JWT Validation Design

1. **Password Hashing**: User passwords are encrypted using the `BCrypt` hashing algorithm with a standard strength coefficient (salt rounds).
2. **JWT Issuance**: Upon successful login or registration, the service signs a JWT with the `HS256` signature algorithm. The token payload encodes the user's UUID and username.
3. **Secret Token Key**: Configured via the `JWT_SECRET` environment variable. Ensure this matches the value configured in the API Gateway.
