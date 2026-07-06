# Discovery Server (Local Development Only)

The **Discovery Server** is a Netflix Eureka instance used for dynamic service registration and client-side load balancing during local development.

---

## 1. Local Development Role

* **Port**: `8761`
* **Local Console**: `http://localhost:8761`
* In a local `docker-compose` environment, microservices register themselves with Eureka on startup and fetch routing tables.

---

## 2. Kubernetes Cluster Bypass (Direct Routing)

When deploying this application stack to a **Kubernetes cluster**, the Discovery Server is bypassed:

1. **Reason**: Running Eureka introduces a single point of failure, extra heartbeat network chatter, and adds 256Mi–384Mi of memory overhead.
2. **Implementation**: The Eureka discovery client is disabled in the Kubernetes ConfigMap using `EUREKA_CLIENT_ENABLED: "false"`.
3. **Property Delivery**: Inter-service routing is handled natively by the Kubernetes CoreDNS service. Downstream service URLs are directly mapped to their Kubernetes service names (e.g., `http://product-service:8081`), leveraging Kubernetes native ClusterIP load balancing instead of client-side ribbon/load-balancers.
