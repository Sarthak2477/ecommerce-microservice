# Guide: Deploying Microservices on a Kubernetes Playground Using Helm

Kubernetes playgrounds (such as **Killercoda** or **Play with Kubernetes**) are excellent, free learning platforms. They give you a fully functional, multi-node Kubernetes cluster right in your web browser. 

Since these environments are **ephemeral** (they reset completely after 1 to 4 hours), manually typing out and applying 15+ different deployment and service manifests on every session is tedious.

This guide explains how to use the **Helm chart** we created (`ecomm-chart`) to deploy your entire microservices, infrastructure, and observability stack on a playground with a **single command**.

---

## Playground Deployment Flow

```mermaid
graph TD
    Developer[Local Machine] -->|1. Build & Push x86 Images| DockerHub[Docker Hub]
    Developer -->|2. Push Helm Chart & Code| GitHub[GitHub Repo]
    
    subgraph Browser Playground (e.g., Killercoda)
        PlaygroundTerminal[Playground Terminal] -->|3. Git Clone| GitHub
        PlaygroundTerminal -->|4. Run Helm Install| Helm[Helm CLI]
        Helm -->|5. Deploys Everything| K8s[Playground Kubernetes Cluster]
        
        K8s -->|6. Automatic Provisioning| Resources[Postgres, Redis, Kafka, 7 Services, Prometheus, Grafana]
    end
    
    User[Developer Browser] -->|7. Port Access Tab| Grafana[Grafana Port 3000]
    User -->|7. Port Access Tab| Gateway[Ingress Port 80]
```

---

## Step-by-Step Walkthrough

### Step 1: Push your Microservice Docker Images to Docker Hub
Playground clusters run on the public cloud and need to pull your images from a public registry. Before starting your playground session, build and push your images from your local machine.

*(Note: Playgrounds run on standard x86 servers, so standard Docker builds work fine).*

1. Build and push the images using Docker Hub:
   ```bash
   # Log in locally
   docker login
   
   # Build & push each service
   docker build -t yourdockerhubuser/ecomm-api-gateway:latest ./api-gateway
   docker push yourdockerhubuser/ecomm-api-gateway:latest
   
   docker build -t yourdockerhubuser/ecomm-product-service:latest ./product_service
   docker push yourdockerhubuser/ecomm-product-service:latest
   
   # (Repeat for user-service, order-service, inventory-service, payment-service, notification-service)
   ```

2. Make sure you commit your local code changes and the `ecomm-chart` directory and push them to your **GitHub Repository**:
   ```bash
   git add .
   git commit -m "feat: added helm chart"
   git push origin main
   ```

---

### Step 2: Open a Kubernetes Playground
1. Go to [Killercoda Kubernetes Playground](https://killercoda.com/playgrounds/scenario/kubernetes-kubeadm).
2. Start the scenario. You will be logged into an Ubuntu control-plane node terminal.

---

### Step 3: Clone Your Repository in the Playground
In the playground terminal, clone your GitHub repository:
```bash
git clone https://github.com/<your-github-username>/<your-repo-name>.git
cd <your-repo-name>
```

---

### Step 4: Configure `values.yaml`
Before launching, edit `ecomm-chart/values.yaml` to point to your Docker Hub username.
You can use `nano` or `vi` directly in the playground terminal:
```bash
nano ecomm-chart/values.yaml
```
Look for:
```yaml
global:
  image:
    repositoryPrefix: "yourdockerhubuser"  # <-- Change this to your Docker Hub username!
```
Save the file (`Ctrl+O`, then `Enter`, then `Ctrl+X` to exit nano).

---

### Step 5: Install the Helm Chart
1. Check if Helm is installed (it is pre-installed on Killercoda):
   ```bash
   helm version
   ```
2. Deploy the entire stack into a namespace:
   ```bash
   # Create namespace
   kubectl create namespace ecomm
   
   # Install the chart
   helm install ecomm ./ecomm-chart --namespace ecomm
   ```

---

### Step 6: Verify the Deployment
Watch the pods spin up:
```bash
kubectl get pods -n ecomm -w
```
*(Wait 1-2 minutes. All pods, including `postgres`, `redis`, `kafka`, your microservices, `prometheus`, and `grafana` should shift to `Running` status).*

---

### Step 7: Accessing the Gateway & Grafana
Kubernetes playgrounds do not have public IPs or routers. Instead, they provide a **Port Access** feature in the browser.

#### 7.1 Accessing the API Gateway (Port 80)
1. On Killercoda, click the **Traffic / Port Access** tab (usually at the top of the interface or under a menu).
2. Enter Port **80** (which Nginx Ingress Controller exposes) and click access.
3. This opens a URL. Append your API route, for example:
   `https://<playground-url>/api/products` to access your product-service through the gateway!

#### 7.2 Accessing the Grafana Dashboard (Port 3000)
By default, the Grafana service runs inside the cluster. To make it accessible:
1. Port-forward the Grafana service to port `3000` in the playground background:
   ```bash
   kubectl port-forward svc/grafana-service 3000:3000 -n ecomm --address 0.0.0.0 &
   ```
2. Click the **Traffic / Port Access** tab in Killercoda.
3. Enter Port **3000** and click access.
4. You will see the Grafana Login page! Log in with `admin` / `admin` and import your JVM dashboards.

---

## Helm Chart Cheat Sheet (Useful Commands)

If you modify your code during a session:
* **Upgrade the deployment:**
  ```bash
  helm upgrade ecomm ./ecomm-chart --namespace ecomm
  ```
* **View deployment logs:**
  ```bash
  kubectl logs -f deployment/product-service -n ecomm
  ```
* **Uninstall everything (Clean up):**
  ```bash
  helm uninstall ecomm --namespace ecomm
  ```
