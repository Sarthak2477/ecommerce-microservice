# Config Server (Local Development Only)

The **Config Server** acts as a centralized configuration manager during local development. It reads microservice configuration properties from a Git repository or local filesystem and serves them over REST.

---

## 1. Local Development Role

* **Port**: `8888`
* **Local Access**: `http://localhost:8888`
* In a local `docker-compose` setup, microservices load their properties on startup from this container.

---

## 2. Kubernetes Cluster Bypass (Direct Routing)

When deploying this application stack to a **Kubernetes cluster**, the Config Server is bypassed:

1. **Reason**: Running an extra Java container to host configuration properties adds startup latency and increases the cluster's memory footprint by 256Mi–384Mi.
2. **Implementation**: The config client check is disabled in the Kubernetes ConfigMap using `SPRING_CLOUD_CONFIG_ENABLED: "false"`.
3. **Property Delivery**: Properties are packaged directly into each microservice's Docker image at build time (inside `src/main/resources`), and dynamic overrides (database passwords, endpoints) are injected via standard Kubernetes Environment Variables and ConfigMaps.
