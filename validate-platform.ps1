$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " SIH26069 PLATFORM COMPLETE HEALTH & API VALIDATION" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan

$healthChecks = @(
    @{ Name = "Ingestion Actuator"; Url = "http://localhost:8081/actuator/health" },
    @{ Name = "Citizen Actuator";   Url = "http://localhost:8082/actuator/health" },
    @{ Name = "Verify Actuator";    Url = "http://localhost:8083/actuator/health" },
    @{ Name = "Analytics Actuator"; Url = "http://localhost:8084/actuator/health" },
    @{ Name = "Gateway Actuator";   Url = "http://localhost:8080/actuator/health" }
)

$apiChecks = @(
    @{ Name = "Stations API";       Url = "http://localhost:8080/api/v1/stations" },
    @{ Name = "Events Query";       Url = "http://localhost:8080/api/v1/events" },
    @{ Name = "Alerts Query";       Url = "http://localhost:8080/api/v1/alerts" },
    @{ Name = "Active Alerts";      Url = "http://localhost:8080/api/v1/alerts/active" },
    @{ Name = "Citizen Reports";    Url = "http://localhost:8080/api/v1/reports" },
    @{ Name = "Verify Metrics";     Url = "http://localhost:8080/api/v1/verify/metrics" },
    @{ Name = "District Anomalies"; Url = "http://localhost:8080/api/v1/analytics/anomalies" },
    @{ Name = "System Stats";       Url = "http://localhost:8080/api/v1/analytics/system-stats" },
    @{ Name = "CAP 1.2 XML Feed";   Url = "http://localhost:8080/api/v1/alerts/feed/cap" }
)

$passed = 0
$total = $healthChecks.Count + $apiChecks.Count

Write-Host "`n[1] Checking Microservice Actuator Health Endpoints..." -ForegroundColor Yellow
foreach ($item in $healthChecks) {
    try {
        $response = Invoke-WebRequest -Uri $item.Url -TimeoutSec 5 -UseBasicParsing
        Write-Host ("PASS  {0,-22} HTTP {1}" -f $item.Name, $response.StatusCode) -ForegroundColor Green
        $passed++
    }
    catch {
        Write-Host ("FAIL  {0,-22} {1}" -f $item.Name, $_.Exception.Message) -ForegroundColor Red
    }
}

Write-Host "`n[2] Checking API Gateway Reverse Proxy Routes..." -ForegroundColor Yellow
foreach ($item in $apiChecks) {
    try {
        $response = Invoke-WebRequest -Uri $item.Url -TimeoutSec 5 -UseBasicParsing
        Write-Host ("PASS  {0,-22} HTTP {1}" -f $item.Name, $response.StatusCode) -ForegroundColor Green
        $passed++
    }
    catch {
        Write-Host ("FAIL  {0,-22} {1}" -f $item.Name, $_.Exception.Message) -ForegroundColor Red
    }
}

Write-Host "`n[3] Testing AI Ingestion Pipeline (POST /api/v1/events/ai)..." -ForegroundColor Yellow
$aiPayload = @{
    eventId = "event-val-test-" + (Get-Random -Minimum 1000 -Maximum 9999)
    eventType = "FLOOD"
    source = "AI_ANALYSIS"
    location = @{
        city = "Mumbai"
        state = "Maharashtra"
        latitude = 19.0760
        longitude = 72.8777
    }
    severity = "HIGH"
    confidence = 94.0
    reportCount = 100
    summary = "Validation pipeline flood event test"
} | ConvertTo-Json

try {
    $aiRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/events/ai" -Method Post -Body $aiPayload -ContentType "application/json" -TimeoutSec 5
    Write-Host ("PASS  {0,-22} Status: {1}" -f "AI Event Ingestion", $aiRes.status) -ForegroundColor Green
    $passed++
    $total++
}
catch {
    Write-Host ("FAIL  {0,-22} {1}" -f "AI Event Ingestion", $_.Exception.Message) -ForegroundColor Red
    $total++
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " RESULT: $passed / $total CHECKS PASSED" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
