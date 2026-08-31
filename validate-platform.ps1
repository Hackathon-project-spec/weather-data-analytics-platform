$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " SIH26069 PLATFORM QUICK VALIDATION" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan

$urls = @(
@{ Name = "Ingestion"; Url = "http://localhost:8081/api/v1/stations" },
@{ Name = "Citizen"; Url = "http://localhost:8082/api/v1/reports" },
@{ Name = "Verification"; Url = "http://localhost:8083/api/v1/verify/metrics" },
@{ Name = "Analytics"; Url = "http://localhost:8084/api/v1/analytics/anomalies" },
@{ Name = "Gateway"; Url = "http://localhost:8080/api/v1/stations" },
@{ Name = "Frontend"; Url = "http://localhost:3000" }
)

$passed = 0
$total = $urls.Count

Write-Host ""
Write-Host "Checking services..." -ForegroundColor Yellow
Write-Host ""

foreach ($item in $urls) {
try {
$response = Invoke-WebRequest -Uri $item.Url -TimeoutSec 5 -UseBasicParsing
Write-Host ("PASS  {0,-15} HTTP {1}" -f $item.Name, $response.StatusCode) -ForegroundColor Green
$passed++
}
catch {
Write-Host ("FAIL  {0,-15} {1}" -f $item.Name, $_.Exception.Message) -ForegroundColor Red
}
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " RESULT: $passed / $total SERVICES AVAILABLE" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

if ($passed -eq $total) {
Write-Host "OVERALL RESULT: PLATFORM IS UP" -ForegroundColor Green
}
else {
Write-Host "OVERALL RESULT: SOME SERVICES NEED ATTENTION" -ForegroundColor Yellow
}

Write-Host ""
