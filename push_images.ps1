# PowerShell script to build and push all microservices Docker images

# 1. CHANGE THIS to your actual Docker Hub username!
$DOCKER_USER = "sarthak247" 

# List of service directories
$SERVICES = @(
    "api-gateway", 
    "product_service", 
    "order-service", 
    "inventory-service", 
    "user-service", 
    "notification-service", 
    "payment_service"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Step 1: Building all JAR files with Maven..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

foreach ($service in $SERVICES) {
    Write-Host "Building JAR for $service..." -ForegroundColor Yellow
    Push-Location $service
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed for $service. Exiting."
        Pop-Location
        exit 1
    }
    Pop-Location
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Step 2: Building Docker Images..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

docker build -t "$DOCKER_USER/ecomm-api-gateway:latest" ./api-gateway
docker build -t "$DOCKER_USER/ecomm-product-service:latest" ./product_service
docker build -t "$DOCKER_USER/ecomm-order-service:latest" ./order-service
docker build -t "$DOCKER_USER/ecomm-inventory-service:latest" ./inventory-service
docker build -t "$DOCKER_USER/ecomm-user-service:latest" ./user-service
docker build -t "$DOCKER_USER/ecomm-notification-service:latest" ./notification-service
docker build -t "$DOCKER_USER/ecomm-payment-service:latest" ./payment_service

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "Step 3: Pushing Docker Images to Docker Hub..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

docker push "$DOCKER_USER/ecomm-api-gateway:latest"
docker push "$DOCKER_USER/ecomm-product-service:latest"
docker push "$DOCKER_USER/ecomm-order-service:latest"
docker push "$DOCKER_USER/ecomm-inventory-service:latest"
docker push "$DOCKER_USER/ecomm-user-service:latest"
docker push "$DOCKER_USER/ecomm-notification-service:latest"
docker push "$DOCKER_USER/ecomm-payment-service:latest"

Write-Host "`n==========================================" -ForegroundColor Green
Write-Host "All images successfully pushed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
