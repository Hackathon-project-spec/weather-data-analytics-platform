@echo off
title SIH26069 National Weather Big Data Analytics Platform
echo =================================================================
echo   Starting SIH26069 National Weather Big Data Analytics Platform
echo   Ministry of Earth Sciences (MoES) Prototype
echo =================================================================

set "ROOT=%~dp0"
set "BACKEND=%ROOT%weather-platform-backend"
set "FRONTEND=%ROOT%weather-platform-frontend"

echo [1/5] Starting Ingestion Service on Port 8081...
start "Ingestion Service (8081)" /D "%BACKEND%" mvn spring-boot:run -pl ingestion-service

echo [2/5] Starting Citizen Service on Port 8082...
start "Citizen Service (8082)" /D "%BACKEND%" mvn spring-boot:run -pl citizen-service

echo [3/5] Starting Verification Engine on Port 8083...
start "Verification Engine (8083)" /D "%BACKEND%" mvn spring-boot:run -pl verification-engine

echo [4/5] Starting Analytics Service on Port 8084...
start "Analytics Service (8084)" /D "%BACKEND%" mvn spring-boot:run -pl analytics-service

echo [5/5] Starting API Gateway on Port 8080...
start "API Gateway (8080)" /D "%BACKEND%" mvn spring-boot:run -pl api-gateway

echo [6/6] Starting React Frontend on Port 3000...
start "React Frontend (3000)" /D "%FRONTEND%" npm run dev

echo.
echo All microservices and frontend launched!
echo Access the Dashboard at: http://localhost:3000
echo Unified API Gateway:    http://localhost:8080
