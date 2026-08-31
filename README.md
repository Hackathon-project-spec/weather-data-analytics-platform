# National Weather Big Data Analytics Platform (SIH26069)
### Ministry of Earth Sciences (MoES) & India Meteorological Department (IMD) Prototype

An enterprise-grade, high-throughput meteorological big data and crowdsourced disaster verification platform built for **Smart India Hackathon 2026 (Problem Statement SIH26069)**.

---

## 🌟 Key Architecture & Capabilities

```
                                  APACHE KAFKA EVENT BACKBONE
  ┌────────────────────────┬────────────────────────┬────────────────────────┬────────────────────────┐
  │ weather.raw.telemetry  │  weather.social.feed   │ weather.citizen.reports│ weather.verified.events│
  │ (AWS Readings / OpenM) │  (Simulated Posts #IMD)│ (Crowdsourced Reports) │ (Scored & Verified)    │
  └───────────▲────────────┴───────────▲────────────┴───────────▲────────────┴───────────▲────────────┘
              │                        │                        │                        │
       [PRODUCES]               [PRODUCES]               [PRODUCES]               [PRODUCES]
              │                        │                        │                        │
  ┌─────────────────────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
  │         ingestion-service               │      │    citizen-service      │      │   verification-engine   │
  │ • Open-Meteo live sync (30+ cities)     │      │ • Submit citizen report │      │ • Consumes Kafka report │
  │ • AWS Simulator (10–1000 events/sec)    │      │ • Postgres CRUD state   │      │ • Redis GEO & ClickHouse│
  │ • Simulated #IMD Social Stream          │      │ • Upvoting & reputation │      │ • Prototype Score Engine│
  │ • Scenario Lab (Mumbai, Odisha, Delhi..)│      │ • Tracks status         │      │ • Measures latency (ms) │
  └─────────────────────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
                                                                                                 │
                                                                                          [PRODUCES/CONSUMES]
                                                                                                 │
  ┌─────────────────────────────────────────┐      ┌─────────────────────────────────────────────▼────────────┐
  │             api-gateway                 │      │                    analytics-service                     │
  │ • Unified entry point (:8080)           │      │ • ClickHouse OLAP Sink & Aggregator                      │
  │ • Reverse proxy to microservices        │      │ • Extreme anomaly detection (Rain/Heat/Pressure)         │
  │ • Spring STOMP WebSocket broker         │      │ • CAP (Common Alerting Protocol) generator               │
  │ • Latency & Throughput monitor          │      │ • Redis Active Alert cache                               │
  └─────────────────────────────────────────┘      └──────────────────────────────────────────────────────────┘
```

1. **5 Genuine Independently Running Microservices (Java 21 / Spring Boot 3.3)**:
   - **`api-gateway` (Port 8080)**: Reverse proxy router and Spring STOMP WebSocket broker.
   - **`ingestion-service` (Port 8081)**: Ingests live Open-Meteo telemetry for 30+ Indian metropolitan hubs, drives high-speed AWS simulation (10–1000 events/sec), streams simulated `#IMD` social posts, and executes 4 demonstration scenarios.
   - **`citizen-service` (Port 8082)**: Citizen disaster report CRUD, PostgreSQL persistence, upvotes, and lifecycle tracking.
   - **`verification-engine` (Port 8083)**: Spatial-temporal cross-verification against nearby AWS sensor ground truth, calculating prototype confidence score (0–100%) with transparent breakdown and reasoning in sub-second latency.
   - **`analytics-service` (Port 8084)**: ClickHouse OLAP time-series aggregations, district anomaly detection, and Common Alerting Protocol (CAP) emergency alert feeds.
2. **Hybrid Storage Strategy**:
   - **PostgreSQL**: Relational state, reports lifecycle, audit logs, and CAP alerts.
   - **ClickHouse**: Columnar OLAP time-series database for sub-second analytical queries across millions of telemetry points.
   - **Redis**: Geospatial radius indexing (`geo:stations`) and active alert cache.
3. **Transparent Prototype Scoring Breakdown**:
   - **Sensor Match (0–40 pts)**: Correlation with physical sensors.
   - **Spatial Proximity (0–25 pts)**: Inverse distance penalty ($<5\text{ km} \rightarrow 25\text{ pts}$, $>35\text{ km} \rightarrow 2\text{ pts}$).
   - **Temporal Alignment (0–15 pts)**: Alignment within $\le 30\text{ minutes}$.
   - **Social Corroboration (0–10 pts)**: Simulated `#IMD` social signals in the same district.
   - **Consensus & Upvotes (0–10 pts)**: Community peer validation.
4. **4 Demonstrable Scenarios in Scenario Lab**:
   - **Scenario A (Mumbai Cloudburst)**: Injects 95–115 mm/hr extreme rainfall $\rightarrow$ auto-verifies flood reports (96%) $\rightarrow$ triggers Red Alert.
   - **Scenario B (Odisha Super Cyclone)**: Injects 125 km/h gale winds and 980 hPa barometric drop $\rightarrow$ triggers Cyclone Warning.
   - **Scenario C (Delhi Extreme Heatwave)**: Injects 47.8°C temperatures $\rightarrow$ triggers Heat Index Emergency.
   - **Scenario D (Coordinated Fake Disaster Reports)**: Injects false Blizzard claims in 34.5°C Chennai $\rightarrow$ demonstrates automated physical refutation and **DEBUNKED (12%)** rejection.

---

## 🚀 How to Run the Platform

### Option A: One-Click Startup Script (Recommended)

```powershell
# From project root:
.\run-all.ps1
```
Or double-click `run-all.bat` on Windows.

### Option B: Docker Compose (when Docker daemon is active)

```bash
cd docker
docker compose up -d
```

### Option C: Manual Launch

1. **Build Backend**:
   ```bash
   cd weather-platform-backend
   mvn clean install -DskipTests
   ```
2. **Start Microservices**:
   - Ingestion: `mvn spring-boot:run -pl ingestion-service` (Port 8081)
   - Citizen: `mvn spring-boot:run -pl citizen-service` (Port 8082)
   - Verification: `mvn spring-boot:run -pl verification-engine` (Port 8083)
   - Analytics: `mvn spring-boot:run -pl analytics-service` (Port 8084)
   - Gateway: `mvn spring-boot:run -pl api-gateway` (Port 8080)
3. **Start Frontend**:
   ```bash
   cd weather-platform-frontend
   npm install
   npm run dev
   ```

Open **http://localhost:3000** in your browser.

---

## 🧪 Testing the Prototype

Run all automated unit & integration tests across the microservices:

```bash
cd weather-platform-backend
mvn test
```
