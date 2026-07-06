# Guide: Deploying Microservices on AWS Free-Tier (EC2 + K3s + CI/CD + Observability)

Deploying a multi-service microservice ecosystem (9 Spring Boot applications + Postgres + Redis + Kafka + Prometheus + Grafana) on a **free-tier AWS EC2 instance** is an excellent learning journey. 

However, it presents a major technical challenge: **RAM constraints**. A free-tier EC2 instance (`t2.micro` or `t3.micro`) has only **1 GB of RAM**. Running this stack out-of-the-box requires at least **8 GB of RAM**.

This guide provides a step-by-step walkthrough of the architectural optimizations, server configurations, and deployment steps needed to make this fit into a resource-constrained environment, and how to scale it when ready.

---

## The Architecture Flow

```mermaid
graph TD
    Developer[Developer Push] -->|1. Commit & Push| GitHub[GitHub Repo]
    GitHub -->|2. Trigger Workflow| GHA[GitHub Actions]
    GHA -->|3. Build & Push Image| DockerHub[Docker Hub]
    GHA -->|4. Deploy Manifests via SSH| EC2[EC2 Instance - Ubuntu]
    
    subgraph EC2 Instance (K3s Kubernetes Cluster)
        Traefik[Traefik Ingress Controller] -->|Routes Port 80/443| SCG[Spring Cloud Gateway]
        
        subgraph Microservices Namespace
            SCG -->|Routes API Requests| US[user-service]
            SCG -->|Routes API Requests| PS[product-service]
            SCG -->|Routes API Requests| OS[order-service]
            SCG -->|Routes API Requests| IS[inventory-service]
            SCG -->|Routes API Requests| PayS[payment-service]
            SCG -->|Routes API Requests| NS[notification-service]
        end
        
        subgraph Infrastructure Namespace
            OS & PayS & NS -->|Pub/Sub Events| Kafka[Lightweight KRaft Kafka]
            US & PS & OS & IS & PayS -->|Read/Write| Postgres[(PostgreSQL)]
            SCG & OS -->|Caching / Session| Redis[(Redis)]
        end
        
        subgraph Observability Namespace
            Prom[Prometheus] -->|Scrapes Metrics /actuator/prometheus| US & PS & OS & IS & PayS & NS & SCG
            Grafana[Grafana Dashboard] -->|Queries| Prom
        end
    end
```

---

## Table of Contents
1. [The Memory Optimization Strategy (Crucial)](#the-memory-optimization-strategy-crucial)
2. [Step 1: Setting up the AWS EC2 Instance](#step-1-setting-up-the-aws-ec2-instance)
3. [Step 2: Installing K3s and Configuring Swap Space](#step-2-installing-k3s-and-configuring-swap-space)
4. [Step 3: Creating Kubernetes Namespace and ConfigMaps](#step-3-creating-kubernetes-namespace-and-configmaps)
5. [Step 4: Deploying Infrastructure (Postgres, Redis, Kafka KRaft)](#step-4-deploying-infrastructure-postgres-redis-kafka-kraft)
6. [Step 5: Deploying Microservices & Gateway](#step-5-deploying-microservices--gateway)
7. [Step 6: Setting up the CI/CD Pipeline (GitHub Actions)](#step-6-setting-up-the-cicd-pipeline-github-actions)
8. [Step 7: Setting up Ingress & Routing (Traefik)](#step-7-setting-up-ingress--routing-traefik)
9. [Step 8: Deploying Prometheus and Grafana](#step-8-deploying-prometheus-and-grafana)

---

## The Memory Optimization Strategy (Crucial)

> [!WARNING]
> If you deploy these services with default configurations, the EC2 instance will freeze and crash immediately due to Out-Of-Memory (OOM) errors. We must use these 5 optimization techniques:

1. **Kubernetes-Native Service Discovery (No Eureka):** In Kubernetes, we do not need `discovery-server` (Eureka). Kubernetes has a built-in DNS service (`CoreDNS`). A service named `product-service` is accessible to other pods at `http://product-service:8081`. Removing Eureka saves **~250MB RAM**.
2. **Kubernetes-Native Config Management (No Config Server):** Instead of running a Spring Cloud Config Server, we will store configuration values in a Kubernetes `ConfigMap` and inject them as environment variables. This saves another **~250MB RAM**.
3. **Aggressive JVM Memory Tuning:** We will force each Spring Boot service to use a maximum heap size of `128MB` or `192MB` using JVM flags (`-Xmx128m -XX:+UseG1GC`).
4. **ZooKeeperless Kafka (KRaft Mode):** Standard Kafka requires ZooKeeper (two separate heavy containers). We will deploy a single-node Kafka in **KRaft mode**, combining them into one lightweight container and limiting its JVM memory to `192MB`.
5. **Configuring a Swap File (Virtual RAM):** We will configure **4 GB of Swap space** on the EC2 SSD. When the physical 1GB RAM is full, the OS will offload less active processes to the disk. *Note: Disk access is slower than RAM, but it keeps the server running for free!*

---

## Step 1: Setting up the AWS EC2 Instance

### 1.1 Create the EC2 Instance
1. Log in to your **AWS Management Console**.
2. Search for **EC2** and click **Launch Instance**.
3. **Name**: `ecomm-k3s-server`
4. **OS (AMI)**: Select **Ubuntu 22.04 LTS** (Free tier eligible).
5. **Instance Type**: Select **t2.micro** (1 vCPU, 1 GiB RAM) or **t3.micro** (if available in your region's free tier).
   > [!TIP]
   > If you have a few dollars to spare, selecting a **t3.medium** (2 vCPU, 4 GiB RAM) costs around **$0.04/hour** (~$1.00 for 24 hours of use) and will make the deployment much smoother and faster. You can stop the instance when you are not using it to avoid charges.
6. **Key Pair**: Click **Create new key pair**.
   - Key pair name: `ecomm-key`
   - Private key file format: `.pem` (for SSH)
   - Save the downloaded `ecomm-key.pem` file safely.
7. **Network Settings**:
   - Check **Allow SSH traffic from** (Select "My IP" for security, or "Anywhere" for learning).
   - Check **Allow HTTP traffic from the internet**.
   - Check **Allow HTTPS traffic from the internet**.

### 1.2 Edit Security Group Inbound Rules
Kubernetes needs some additional ports open for Grafana/Prometheus and service access.
1. Scroll down to **Network settings** -> click **Edit**.
2. Add the following inbound rules:

| Port Range | Protocol | Source | Description |
| :--- | :--- | :--- | :--- |
| `22` | TCP | My IP | SSH Access |
| `80` | TCP | Anywhere (0.0.0.0/0) | HTTP (API Gateway Ingress) |
| `443` | TCP | Anywhere (0.0.0.0/0) | HTTPS |
| `6443` | TCP | My IP | Kubernetes API (for local kubectl access) |
| `3000` | TCP | Anywhere / My IP | Grafana Dashboard (optional, or we can use Ingress) |

3. Click **Launch Instance**.

---

## Step 2: Installing K3s and Configuring Swap Space

Once the instance status is "Running", find its **Public IP Address**.

### 2.1 SSH into your EC2 Instance
Open your terminal (Git Bash, Command Prompt, or PowerShell) and run:
```bash
# Change permissions of key file (Linux/Mac only)
chmod 400 /path/to/ecomm-key.pem

# SSH into the Ubuntu machine
ssh -i "/path/to/ecomm-key.pem" ubuntu@<YOUR_EC2_PUBLIC_IP>
```

### 2.2 Configure Swap Space (Virtual RAM)
Run these commands one by one to create a **4 GB Swap File**:
```bash
# Create a 4GB file
sudo fallocate -l 4G /swapfile

# Set correct permissions
sudo chmod 600 /swapfile

# Format it as swap space
sudo mkswap /swapfile

# Enable the swap
sudo swapon /swapfile

# Make the swap permanent across reboots
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify it works (should show 1GB RAM + 4GB Swap)
free -h
```

### 2.3 Install K3s (Lightweight Kubernetes)
K3s is a highly optimized, fully certified Kubernetes distribution created by Rancher. It is perfect for low-memory environments.
Run the installer command:
```bash
curl -sfL https://get.k3s.io | sh -
```
Verify the installation:
```bash
# Check if cluster is running
sudo kubectl get nodes
```
To run kubectl without typing `sudo` every time:
```bash
sudo chmod 644 /etc/rancher/k3s/k3s.yaml
mkdir -p ~/.kube
cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
```

---

## Step 3: Creating Kubernetes Namespace and ConfigMaps

We will place all our configurations in a single place: a Kubernetes `ConfigMap`. This replaces the Spring Cloud Config Server.

Create a folder on your local machine or write the files directly on the EC2 server. Let's create a directory called `k8s-manifests` on your EC2 instance.
```bash
mkdir -p ~/k8s-manifests
```

### 3.1 Namespace Manifest (`namespace.yaml`)
Create `~/k8s-manifests/namespace.yaml`:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ecomm
```
Apply it:
```bash
kubectl apply -f ~/k8s-manifests/namespace.yaml
```

### 3.2 Configurations ConfigMap (`configmap.yaml`)
Create `~/k8s-manifests/configmap.yaml`. This file contains all the properties that our microservices need.
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ecomm-config
  namespace: ecomm
data:
  # Database configuration
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres-service:5432/postgres"
  SPRING_DATASOURCE_USERNAME: "postgres"
  SPRING_DATASOURCE_PASSWORD: "secret"
  
  # Redis configuration
  SPRING_DATA_REDIS_HOST: "redis-service"
  SPRING_DATA_REDIS_PORT: "6379"
  
  # Kafka Configuration
  SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
  
  # Gateway config & routes (Eureka is bypassed - we route directly to K8s service DNS names!)
  JWT_SECRET: "qHaRsJX5t4bndubo4eH5LZbVIjzn5lpQRHqvrEZuLhBdY4L5"
  
  # Feign Clients config (override Eureka names with direct DNS URLs)
  SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_PRODUCT_SERVICE_URL: "http://product-service:8081"
  SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_INVENTORY_SERVICE_URL: "http://inventory-service:8083"
  
  # JVM Tuning flags to save memory (Aggressive limit of 128MB/192MB)
  JVM_OPTS_MED: "-Xms64m -Xmx128m -XX:+UseG1GC -XX:ActiveProcessorCount=1"
  JVM_OPTS_HIGH: "-Xms96m -Xmx192m -XX:+UseG1GC -XX:ActiveProcessorCount=1"
```
Apply it:
```bash
kubectl apply -f ~/k8s-manifests/configmap.yaml
```

---

## Step 4: Deploying Infrastructure (Postgres, Redis, Kafka KRaft)

We will deploy lightweight, single-replica versions of PostgreSQL, Redis, and a ZooKeeperless Kafka.

### 4.1 PostgreSQL (`postgres.yaml`)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:16-alpine
        env:
        - name: POSTGRES_DB
          value: "postgres"
        - name: POSTGRES_USER
          value: "postgres"
        - name: POSTGRES_PASSWORD
          value: "secret"
        ports:
        - containerPort: 5432
        resources:
          limits:
            memory: "200Mi"
            cpu: "0.5"
          requests:
            memory: "100Mi"
            cpu: "0.1"
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: ecomm
spec:
  ports:
  - port: 5432
    targetPort: 5432
  selector:
    app: postgres
```

### 4.2 Redis (`redis.yaml`)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          limits:
            memory: "64Mi"
            cpu: "0.2"
          requests:
            memory: "32Mi"
            cpu: "0.05"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: ecomm
spec:
  ports:
  - port: 6379
    targetPort: 6379
  selector:
    app: redis
```

### 4.3 Kafka (KRaft Mode - `kafka.yaml`)
Using KRaft mode saves running ZooKeeper, saving ~150MB of RAM.
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: apache/kafka:3.7.0
        ports:
        - containerPort: 9092
        env:
        - name: KAFKA_NODE_ID
          value: "1"
        - name: KAFKA_PROCESS_ROLES
          value: "broker,controller"
        - name: KAFKA_LISTENERS
          value: "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://kafka-service:9092"
        - name: KAFKA_CONTROLLER_LISTENER_NAMES
          value: "CONTROLLER"
        - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
          value: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT"
        - name: KAFKA_CONTROLLER_QUORUM_VOTERS
          value: "1@localhost:9093"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
        - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
          value: "1"
        - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
          value: "1"
        - name: KAFKA_LOG_DIRS
          value: "/tmp/kraft-combined-logs"
        # Combine controller and broker metadata log directory
        - name: CLUSTER_ID
          value: "MkU3OEVBNTcwNTJENDM2Qk"
        - name: KAFKA_JVM_PERFORMANCE_OPTS
          value: "-Xms64m -Xmx128m"
        resources:
          limits:
            memory: "256Mi"
            cpu: "0.5"
          requests:
            memory: "128Mi"
            cpu: "0.1"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-service
  namespace: ecomm
spec:
  ports:
  - port: 9092
    targetPort: 9092
  selector:
    app: kafka
```

Apply all infrastructure files:
```bash
kubectl apply -f ~/k8s-manifests/postgres.yaml
kubectl apply -f ~/k8s-manifests/redis.yaml
kubectl apply -f ~/k8s-manifests/kafka.yaml
```

---

## Step 5: Deploying Microservices & Gateway

For each of the Spring Boot applications, we define:
1. A **Deployment** with restricted memory limits and environment configuration mapped from the ConfigMap.
2. A **Service** allowing other pods to reach it.

Here is an example for the **product-service**. Create `~/k8s-manifests/product-service.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        # REPLACE with your Docker Hub username and image tag
        image: yourdockerhubuser/ecomm-product-service:latest
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef:
            name: ecomm-config
        env:
        # Override JVM Options to strictly restrict Memory footprint
        - name: JAVA_TOOL_OPTIONS
          valueFrom:
            configMapKeyRef:
              name: ecomm-config
              key: JVM_OPTS_HIGH
        resources:
          limits:
            memory: "256Mi"
            cpu: "0.5"
          requests:
            memory: "160Mi"
            cpu: "0.1"
---
apiVersion: v1
kind: Service
metadata:
  name: product-service
  namespace: ecomm
spec:
  ports:
  - port: 8081
    targetPort: 8081
  selector:
    app: product-service
```

> [!NOTE]
> You will duplicate this template for all other microservices (`user-service`, `order-service`, `inventory-service`, `payment-service`, `notification-service`), replacing:
> - The metadata name (e.g., `user-service`)
> - The Docker image (e.g., `yourdockerhubuser/ecomm-user-service:latest`)
> - The container port and service port (e.g., `8084` for user-service)
> - JVM memory sizes (`JVM_OPTS_MED` for smaller services like `notification-service`, `JVM_OPTS_HIGH` for larger ones like `order-service`).

---

## Step 6: Setting up the CI/CD Pipeline (GitHub Actions)

We will configure GitHub Actions to:
1. Build the Docker images for all services.
2. Push them to Docker Hub.
3. SSH into the EC2 instance and trigger `kubectl rollout restart deployment/` to load the new images.

### 6.1 Set up Secrets in GitHub
In your GitHub Repository, go to **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret** and add:
- `DOCKERHUB_USERNAME`: Your Docker Hub username.
- `DOCKERHUB_TOKEN`: A Docker Hub Access Token (generate this in Docker Hub Account Settings).
- `EC2_IP`: The public IP of your EC2 instance.
- `EC2_SSH_KEY`: The contents of your `ecomm-key.pem` private key file.

### 6.2 Write GitHub Actions Workflow (`.github/workflows/deploy.yml`)
Create a workflow file in your project:

```yaml
name: CI/CD Pipeline to EC2 K3s

on:
  push:
    branches: [ main ]

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [
          'product_service', 
          'order-service', 
          'inventory-service', 
          'user-service', 
          'notification-service', 
          'payment_service',
          'api-gateway'
        ]
    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'maven'

    - name: Build Jar File
      run: |
        cd ${{ matrix.service }}
        mvn clean package -DskipTests

    - name: Log in to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKERHUB_USERNAME }}
        password: ${{ secrets.DOCKERHUB_TOKEN }}

    - name: Build and Push Docker Image
      uses: docker/build-push-action@v4
      with:
        context: ./${{ matrix.service }}
        push: true
        tags: ${{ secrets.DOCKERHUB_USERNAME }}/ecomm-${{ matrix.service }}:latest

  deploy:
    runs-on: ubuntu-latest
    needs: build-and-push
    steps:
    - name: Deploy to K3s via SSH
      uses: appleboy/ssh-action@master
      with:
        host: ${{ secrets.EC2_IP }}
        username: ubuntu
        key: ${{ secrets.EC2_SSH_KEY }}
        script: |
          # Apply updated deployments (if files changed)
          kubectl apply -f ~/k8s-manifests/
          
          # Force Kubernetes to pull the new 'latest' images
          kubectl rollout restart deployment -n ecomm
```

---

## Step 7: Setting up Ingress & Routing (Traefik)

Instead of exposing our API Gateway directly via NodePort or LoadBalancer (which costs money on AWS), we can use K3s' built-in **Traefik Ingress Controller**.

### 7.1 Spring Cloud Gateway Routes Configuration
We need the API Gateway to route to native Kubernetes service DNS URLs instead of Eureka (`lb://`).
Update your `api-gateway.properties` in your config map or local configuration:

```properties
server.port=8080
jwt.secret=qHaRsJX5t4bndubo4eH5LZbVIjzn5lpQRHqvrEZuLhBdY4L5

# Route: user-service (Direct Kubernetes DNS)
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=http://user-service:8084
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**

# Route: product-service
spring.cloud.gateway.routes[1].id=product-service
spring.cloud.gateway.routes[1].uri=http://product-service:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/products/**

# Route: order-service
spring.cloud.gateway.routes[2].id=order-service
spring.cloud.gateway.routes[2].uri=http://order-service:8082
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/orders/**
spring.cloud.gateway.routes[2].filters[0]=JwtAuthenticationFilter

# Route: inventory-service
spring.cloud.gateway.routes[3].id=inventory-service
spring.cloud.gateway.routes[3].uri=http://inventory-service:8083
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/inventory/**
```

### 7.2 Ingress Manifest (`ingress.yaml`)
This routes incoming traffic from HTTP Port 80 of your EC2 instance into the `api-gateway` pod inside the cluster. Create `~/k8s-manifests/ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ecomm-ingress
  namespace: ecomm
  annotations:
    ingress.kubernetes.io/ssl-redirect: "false" # Set to true once you set up SSL
spec:
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 8080
```
Apply it:
```bash
kubectl apply -f ~/k8s-manifests/ingress.yaml
```

Now, any request sent to `http://<YOUR_EC2_PUBLIC_IP>/api/products` will be routed:
`Public Request` -> `Traefik Ingress (Port 80)` -> `Spring Cloud Gateway (Port 8080)` -> `product-service (Port 8081)`.

---

## Step 8: Deploying Prometheus and Grafana

Observability is key for microservices. We will use a lightweight installation of Prometheus and Grafana.

### 8.1 Prometheus deployment (`prometheus.yaml`)
Create `~/k8s-manifests/prometheus.yaml`:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-server-conf
  namespace: ecomm
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
      - job_name: 'spring-boot-apps'
        metrics_path: '/actuator/prometheus'
        static_configs:
          - targets:
            - 'product-service:8081'
            - 'order-service:8082'
            - 'inventory-service:8083'
            - 'user-service:8084'
            - 'payment-service:8086'
            - 'api-gateway:8080'
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:v2.45.0
        args:
          - "--config.file=/etc/prometheus/prometheus.yml"
          - "--storage.tsdb.path=/prometheus/"
        ports:
        - containerPort: 9090
        volumeMounts:
        - name: config-volume
          mountPath: /etc/prometheus/
        resources:
          limits:
            memory: "256Mi"
            cpu: "0.5"
          requests:
            memory: "128Mi"
            cpu: "0.1"
      volumes:
      - name: config-volume
        configMap:
          name: prometheus-server-conf
---
apiVersion: v1
kind: Service
metadata:
  name: prometheus-service
  namespace: ecomm
spec:
  ports:
  - port: 9090
    targetPort: 9090
  selector:
    app: prometheus
```

### 8.2 Grafana deployment (`grafana.yaml`)
Create `~/k8s-manifests/grafana.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: ecomm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana-oss:10.0.0
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_PASSWORD
          value: "admin" # Replace with a secure password!
        resources:
          limits:
            memory: "200Mi"
            cpu: "0.5"
          requests:
            memory: "100Mi"
            cpu: "0.1"
---
apiVersion: v1
kind: Service
metadata:
  name: grafana-service
  namespace: ecomm
spec:
  type: NodePort
  ports:
  - port: 3000
    targetPort: 3000
    nodePort: 30000 # This makes Grafana accessible at http://<YOUR_EC2_PUBLIC_IP>:30000
  selector:
    app: grafana
```

Apply observability manifests:
```bash
kubectl apply -f ~/k8s-manifests/prometheus.yaml
kubectl apply -f ~/k8s-manifests/grafana.yaml
```

### 8.3 Connecting Grafana to Prometheus
1. Wait a couple of minutes for Grafana to start.
2. Open your web browser and navigate to `http://<YOUR_EC2_PUBLIC_IP>:30000`.
3. Log in with Username: `admin` and Password: `admin` (it will prompt you to change it).
4. Go to **Connections** -> **Data Sources** -> **Add data source**.
5. Select **Prometheus**.
6. Set the URL to: `http://prometheus-service:9090`.
7. Scroll to the bottom and click **Save & Test**. It should show a green checkmark saying "Data source is working".
8. **Add a JVM Dashboard**:
   - Go to Dashboards -> **New** -> **Import**.
   - Enter Dashboard ID `4701` (standard JVM Micrometer Dashboard) or `11378` and click **Load**.
   - Select your Prometheus data source and click **Import**.
   - You now have real-time CPU, Memory, Thread, and GC monitoring for all your Spring Boot services!
