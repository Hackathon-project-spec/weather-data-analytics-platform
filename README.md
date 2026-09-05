# National Weather Big Data Analytics Platform (SIH26069)
### Ministry of Earth Sciences (MoES) & India Meteorological Department (IMD) Operational Backend

An enterprise-grade, high-throughput meteorological big data analytics and crowdsourced disaster verification platform built for **Smart India Hackathon 2026 (Problem Statement SIH26069)**.

---

## 1. 🌟 Target Architecture & Team Responsibilities

```
Multiple External Sources (Satellite, Radar, News, Social, AWS)
                       ↓
                   [AI TEAM]
 (Collection + Analysis + Correlation + Deduplication + Classification + Summarization)
                       ↓
              Structured AI Event
                       ↓
         [ingestion-service :8081]
          (Validation + Idempotency)
                       ↓
          [Kafka: weather.ai.events]
                       ↓
                  [Consumers]
   ┌───────────────────┼───────────────────┐
   ↓                   ↓                   ↓
[Verification]    [Analytics]        [Persistence]
 (Spatial Rules) (Decision Engine) (PostgreSQL / ClickHouse)
   └───────────────────┬───────────────────┘
                       ↓
           [Redis Active Alert Cache]
                       ↓
              [API Gateway :8080]
                       ↓
         [REST APIs + WebSocket STOMP]
                       ↓
                [FRONTEND TEAM]
```

### Team Responsibility Boundaries
- **AI Team**:
  - Collects data from multiple external sources (satellite, radars, news, social media, bulletins).
  - Performs cross-source correlation, NLP classification, and deduplication.
  - Summarizes multiple raw signals into **ONE structured intelligence event**.
  - Provides AI-specific confidence scoring and disaster categorization.
  - Submits structured event to Backend API `POST /api/v1/events/ai`.
- **Backend Team**:
  - Receives and validates incoming structured AI events and citizen submissions.
  - Enforces operational validation, spatial boundaries, and idempotency deduplication.
  - Publishes and consumes events across the Apache Kafka backbone.
  - Manages durable persistence in PostgreSQL, OLAP time-series analytics in ClickHouse, and ultra-fast active alert caching in Redis.
  - Implements deterministic operational verification and alert lifecycle state machines.
  - Exposes unified REST APIs and real-time STOMP WebSockets through API Gateway (`:8080`).
- **Frontend Team**:
  - Consumes backend REST APIs and STOMP WebSocket topics (`/ws/weather-live`).
  - Displays geospatial disaster maps, verification console, and scenario lab triggers.
  - Renders time-series charts, anomaly breakdowns, and citizen reporting interfaces.

---

## 2. 🛰️ Microservices Architecture

| Microservice | Port | Primary Responsibilities |
|---|---|---|
| **`api-gateway`** | `8080` | Reverse proxy router, CORS negotiation, Spring STOMP WebSocket broker (`/ws/weather-live`), health monitoring |
| **`ingestion-service`** | `8081` | AI event ingestion (`POST /api/v1/events/ai`), validation, idempotency guard, telemetry simulator, station catalog |
| **`citizen-service`** | `8082` | Citizen crowdsourced disaster reports, PostgreSQL persistence, report upvoting, verification status sync |
| **`verification-engine`** | `8083` | Deterministic physical ground-truth sensor cross-matching (0–100 score), contradiction detection, audit logging |
| **`analytics-service`** | `8084` | Operational decision lifecycle, PostgreSQL event/alert queries, ClickHouse OLAP analytics, Redis active alert caching, CAP 1.2 XML feed |
| **`common-model`** | *Library* | Shared DTOs (`AiEventDTO`, `GeoLocation`, `SystemStatsDTO`, `TimeSeriesPoint`), Enums, and Kafka Event contracts |

*Note: Service discovery is implemented via Docker Compose network DNS names and fixed port mappings without heavyweight Eureka dependency.*

---

## 3. 🗄️ Database Responsibilities

- **PostgreSQL (`weather_platform` database)**:
  - **Durable single source of truth** for transactional data:
    - `ai_events`: Ingested AI events, geolocation, severity, confidence, operational status.
    - `weather_alerts`: CAP-compliant emergency alerts, affected districts, polygons, effective and expiry timestamps.
    - `citizen_reports`: Crowdsourced citizen submissions, GPS coordinates, category, verification status, and upvotes.
    - `stations`: Master registry of 30+ physical AWS/Radar stations across India.
    - `verification_logs`: Factor-by-factor scoring breakdown and audit trails.
  - *All hardcoded application mock responses have been eliminated; all create/read/filter queries interact directly with PostgreSQL repositories.*
- **ClickHouse (`weather_db` database)**:
  - **High-throughput columnar time-series storage & OLAP**:
    - `raw_telemetry`: High-frequency sensor stream (temperature, humidity, pressure, rainfall, wind).
    - `hourly_aggregates`: SummingMergeTree aggregates for rainfall accumulation and temperature trends.
    - `ai_events_analytics`: Long-term time-series tracking of AI disaster occurrences by severity and region.
- **Redis (`weather-redis:6379`)**:
  - **In-memory cache for ultra-low latency reads**:
    - `weather:alerts:active`: Fast-access hash of active emergency alerts.
    - Idempotency key sets and geospatial caches.

---

## 4. 📬 Kafka Topics

| Topic | Producer | Consumer(s) | Message Contract |
|---|---|---|---|
| `weather.ai.events` | `ingestion-service` | `analytics-service`, `api-gateway` | `AiEventDTO` |
| `weather.raw.telemetry` | `ingestion-service` | `analytics-service`, `api-gateway` | `TelemetryEvent` |
| `weather.social.feed` | `ingestion-service` | `verification-engine`, `api-gateway` | `SocialFeedEvent` |
| `weather.citizen.reports` | `citizen-service` | `verification-engine`, `api-gateway` | `CitizenReportEvent` |
| `weather.verified.events` | `verification-engine` | `citizen-service`, `api-gateway` | `VerifiedReportEvent` |
| `weather.alerts.broadcast` | `analytics-service` | `api-gateway`, `analytics-service` | `WeatherAlertEvent` |

---

## 5. 🔌 Complete API Endpoint Reference

All endpoints are accessible directly through **API Gateway (`http://localhost:8080`)**:

| Method | Endpoint | Purpose | Target Service |
|---|---|---|---|
| **POST** | `/api/v1/events/ai` | Ingest structured AI event intelligence *(Preferred)* | `ingestion-service:8081` |
| **POST** | `/api/v1/ingestion/events` | Alias for AI event ingestion | `ingestion-service:8081` |
| **GET** | `/api/v1/events` | Query persisted AI events (filter by `eventType`, `severity`, `status`, `state`, `city`) | `analytics-service:8084` |
| **GET** | `/api/v1/events/{id}` | Get specific AI event by ID | `analytics-service:8084` |
| **GET** | `/api/v1/alerts` | Query weather disaster alerts (filter by `severity`, `category`, `state`, `active`) | `analytics-service:8084` |
| **GET** | `/api/v1/alerts/active` | Get currently active emergency alerts (Redis cache backed) | `analytics-service:8084` |
| **GET** | `/api/v1/alerts/{id}` | Get specific alert by ID | `analytics-service:8084` |
| **GET** | `/api/v1/alerts/feed/cap` | CAP 1.2 compliant XML emergency alert broadcast feed | `analytics-service:8084` |
| **GET** | `/api/v1/stations` | Catalog of active AWS / Radar weather stations across India | `ingestion-service:8081` |
| **POST** | `/api/v1/reports` | Submit citizen crowdsourced disaster report | `citizen-service:8082` |
| **GET** | `/api/v1/reports` | Query citizen reports (filter by `status`, `state`) | `citizen-service:8082` |
| **GET** | `/api/v1/reports/{id}` | Get specific citizen report by ID | `citizen-service:8082` |
| **POST** | `/api/v1/reports/{id}/upvote` | Upvote a citizen report | `citizen-service:8082` |
| **POST** | `/api/v1/verify/evaluate` | Deterministic verification evaluation of a report | `verification-engine:8083` |
| **GET** | `/api/v1/verify/metrics` | Ground-truth verification engine accuracy and latency KPIs | `verification-engine:8083` |
| **GET** | `/api/v1/analytics/system-stats` | Real-time system health, ingestion throughput, and KPI stats | `analytics-service:8084` |
| **GET** | `/api/v1/analytics/timeseries` | Station time-series trend (`stationId`, `range=24h`) | `analytics-service:8084` |
| **GET** | `/api/v1/analytics/anomalies` | Detected district-level weather anomalies | `analytics-service:8084` |
| **GET** | `/api/v1/analytics/severity` | Severity breakdown counts (`EXTREME`, `HIGH`, `MODERATE`, `LOW`) | `analytics-service:8084` |
| **GET** | `/api/v1/analytics/regions` | Regional/state disaster summary statistics | `analytics-service:8084` |
| **POST** | `/api/v1/simulator/start` | Start live telemetry simulation engine | `ingestion-service:8081` |
| **POST** | `/api/v1/simulator/stop` | Stop live telemetry simulation engine | `ingestion-service:8081` |

---

## 6. 🤖 AI Team Integration Contract

The AI team should submit **ONE structured intelligence event** representing its combined analysis:

### Endpoint
`POST http://localhost:8080/api/v1/events/ai` (or `http://localhost:8081/api/v1/events/ai`)  
`Content-Type: application/json`

### Request Payload Example
```json
{
  "eventId": "AI-2026-0001",
  "eventType": "FLOOD",
  "source": "AI_ANALYSIS",
  "location": {
    "city": "Mumbai",
    "state": "Maharashtra",
    "latitude": 19.0760,
    "longitude": 72.8777
  },
  "severity": "HIGH",
  "confidence": 94.0,
  "reportCount": 100,
  "summary": "Multiple correlated external sources confirm severe urban waterlogging and rising water levels.",
  "observedAt": "2026-09-05T12:30:00Z",
  "processedAt": "2026-09-05T12:30:05Z",
  "metadata": {
    "sources": ["twitter", "telegram", "local_news"],
    "model": "WeatherGemini-Pro-v2"
  }
}
```

### Response Payload Example
```json
{
  "status": "INGESTED",
  "eventId": "AI-2026-0001",
  "kafkaPublished": true,
  "correlationId": "48bfa2e1-8891-4cf5-a8c6-eef78b9b2190",
  "timestamp": "2026-09-05T12:30:06.124Z"
}
```
*Duplicate Event Handling*: If an event with the same `eventId` is sent again, the backend responds idempotently with `status: "DUPLICATE_ACCEPTED"` and prevents duplicate downstream pipeline processing.

---

## 7. 💻 Frontend Team Integration Contract

### Event Lifecycle Statuses
`RECEIVED` ➔ `MONITORING` ➔ `ACTIVE_ALERT` / `SUSPICIOUS` / `DEBUNKED` ➔ `ARCHIVED`

### WebSocket STOMP Live Alerts & Data
- **STOMP Endpoint**: `ws://localhost:8080/ws/weather-live` (SockJS fallback: `http://localhost:8080/ws/weather-live`)
- **Subscription Topics**:
  - `/topic/alerts`: Live emergency weather disaster alerts.
  - `/topic/events`: Real-time stream of newly ingested AI intelligence events.
  - `/topic/reports`: Real-time crowdsourced citizen report submissions.
  - `/topic/verified`: Instant physical sensor ground-truth verification outcomes.
  - `/topic/telemetry`: High-frequency AWS telemetry ticks.
  - `/topic/system-stats`: Health, ingestion throughput, and verification KPIs broadcasted every 2 seconds.

---

## 8. 🛠️ Environment Variables & Configuration

| Variable | Default Value | Description |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker bootstrap address |
| `POSTGRES_HOST` | `localhost` | PostgreSQL host |
| `POSTGRES_PORT` | `5432` | PostgreSQL port |
| `POSTGRES_USER` | `postgres` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `postgrespassword` | PostgreSQL password |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis server host |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis server port |
| `CLICKHOUSE_URL` | `jdbc:ch://localhost:8123/weather_db` | ClickHouse JDBC connection URL |
| `CLICKHOUSE_USER` | `default` | ClickHouse username |
| `CLICKHOUSE_PASSWORD` | *(empty)* | ClickHouse password |
| `INGESTION_SERVICE_URL` | `http://localhost:8081` | Ingestion service location for gateway |
| `CITIZEN_SERVICE_URL` | `http://localhost:8082` | Citizen service location for gateway |
| `VERIFICATION_SERVICE_URL` | `http://localhost:8083` | Verification engine location for gateway |
| `ANALYTICS_SERVICE_URL` | `http://localhost:8084` | Analytics service location for gateway |

---

## 9. 🚀 Running the Platform

### Option A: Complete Docker Stack (Infra + Services)
```bash
cd docker
docker compose --profile all up -d --build
```

### Option B: Docker Infrastructure + Local Microservices
1. **Start Core Infrastructure**:
   ```bash
   cd docker
   docker compose up -d
   ```
2. **Build and Package Backend**:
   ```bash
   cd weather-platform-backend
   mvn clean package
   ```
3. **Launch Microservices**:
   Run `.\run-all.ps1` from root, or run each in a separate terminal:
   ```bash
   mvn spring-boot:run -pl ingestion-service     # Port 8081
   mvn spring-boot:run -pl citizen-service       # Port 8082
   mvn spring-boot:run -pl verification-engine   # Port 8083
   mvn spring-boot:run -pl analytics-service     # Port 8084
   mvn spring-boot:run -pl api-gateway           # Port 8080
   ```
4. **Validate Platform**:
   ```powershell
   .\validate-platform.ps1
   ```

---

## 10. 🧪 Automated Test Suite

Run all test suites across the multi-module project:
```bash
cd weather-platform-backend
mvn test
```
All 27 automated unit and integration tests validate:
- Schema validation and idempotency (`AiEventValidationTest`, `AiEventIngestionServiceTest`).
- Verification engine deterministic physical sensor matching (`VerificationEngineTest`).
- PostgreSQL persistence and Redis active alert caching (`AiEventConsumerServiceTest`, `EndToEndAiPipelineIntegrationTest`).
- Dynamic stats calculation and REST queries (`AnalyticsControllerTest`, `CitizenReportServiceTest`).
- Reverse proxy routing (`GatewayRoutingControllerTest`).
