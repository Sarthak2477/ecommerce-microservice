# Notification Service

The **Notification Service** is an event-driven consumer responsible for notifying clients (via logs, emails, or SMS) when significant events happen within the platform.

---

## 1. Core Functions

* **Asynchronous Listening**: Subscribes to the `order-placed` topic to track checkout successes.
* **Notification Simulation**: Parses event payloads and formats client-facing notification logs.
* **Loose Coupling**: Completely decoupled from HTTP request loops, operating 100% via Kafka queues.

---

## 2. Technical Stack

* **Core Framework**: Spring Boot 3.x
* **Messaging**: Spring for Apache Kafka (Consumer)
* **Storage/DB**: None (Operates as a lightweight, memory-efficient consumer)

---

## 3. Port Configuration

* **Service Port**: `8085` (Exposes actuator/health checking)
* **Kubernetes Hostname**: `http://notification-service:8085`

---

## 4. Kafka Event Processing Details

The service implements a dedicated Kafka listener bean:

* **Subscribed Topic**: `order-placed`
* **Consumer Group ID**: `notification-group`

### Logic Flow:
1. An order is completed or initialized, triggering `OrderPlacedEvent` on Kafka.
2. The Notification Service consumes the event payload.
3. It extracts user metadata (e.g. `userId`) and order particulars (e.g. `orderId`, `totalPrice`).
4. Logs an outbound notification record:
   ```text
   Sending email notification to user d7b43a9b-1345-4bf6-8cf9-c3d6f147ae5b for order 3b2e5a61-12ef-45fa-a98b-21d3f92023fa
   ```
5. Extensible hooks allow integrating external APIs (such as Twilio for SMS or SendGrid/SMTP for emails).
