# SIH26069: National Weather Big Data Analytics Platform
# Autonomous Startup Script for all 5 Spring Boot Microservices + React Frontend

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  Starting SIH26069 National Weather Big Data Analytics Platform " -ForegroundColor White
Write-Host "  Ministry of Earth Sciences (MoES) Prototype                    " -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Cyan

$backendDir = "$PSScriptRoot\weather-platform-backend"
$frontendDir = "$PSScriptRoot\weather-platform-frontend"

# 1. Start Ingestion Service (Port 8081)
Write-Host "[1/5] Starting Ingestion Service on Port 8081..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl ingestion-service" -WorkingDirectory $backendDir -WindowStyle Minimized

# 2. Start Citizen Service (Port 8082)
Write-Host "[2/5] Starting Citizen Service on Port 8082..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl citizen-service" -WorkingDirectory $backendDir -WindowStyle Minimized

# 3. Start Verification Engine (Port 8083)
Write-Host "[3/5] Starting Verification Engine on Port 8083..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl verification-engine" -WorkingDirectory $backendDir -WindowStyle Minimized

# 4. Start Analytics Service (Port 8084)
Write-Host "[4/5] Starting Analytics Service on Port 8084..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl analytics-service" -WorkingDirectory $backendDir -WindowStyle Minimized

# 5. Start API Gateway (Port 8080)
Write-Host "[5/5] Starting API Gateway on Port 8080..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl api-gateway" -WorkingDirectory $backendDir -WindowStyle Minimized

# 6. Start React Frontend (Port 3000)
Write-Host "[6/6] Starting React Frontend on Port 3000..." -ForegroundColor Cyan
Start-Process -FilePath "npm" -ArgumentList "run dev" -WorkingDirectory $frontendDir -WindowStyle Normal

Write-Host "`nAll services launched successfully!" -ForegroundColor Green
Write-Host "Open Dashboard in Browser: http://localhost:3000" -ForegroundColor Yellow
Write-Host "Unified API Gateway:        http://localhost:8080" -ForegroundColor White
