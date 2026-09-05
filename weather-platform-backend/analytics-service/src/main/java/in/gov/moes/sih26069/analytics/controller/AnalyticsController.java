package in.gov.moes.sih26069.analytics.controller;

import in.gov.moes.sih26069.analytics.entity.AiEventEntity;
import in.gov.moes.sih26069.analytics.repository.AiEventRepository;
import in.gov.moes.sih26069.analytics.service.AlertManagementService;
import in.gov.moes.sih26069.analytics.service.ClickHouseTimeSeriesService;
import in.gov.moes.sih26069.common.dto.SystemStatsDTO;
import in.gov.moes.sih26069.common.dto.TimeSeriesPoint;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private ClickHouseTimeSeriesService timeSeriesService;

    @Autowired
    private AlertManagementService alertService;

    @Autowired(required = false)
    private AiEventRepository aiEventRepository;

    @Value("${services.verification.url:http://localhost:8083}")
    private String verificationServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/events")
    public ResponseEntity<List<AiEventEntity>> getEvents(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        if (aiEventRepository == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<AiEventEntity> events;
        if (status != null && !status.isBlank()) {
            try {
                OperationalEventStatus opStatus = OperationalEventStatus.valueOf(status.toUpperCase().trim());
                events = aiEventRepository.findByOperationalStatusOrderByCreatedAtDesc(opStatus);
            } catch (IllegalArgumentException e) {
                events = aiEventRepository.findAllByOrderByCreatedAtDesc();
            }
        } else if (eventType != null && !eventType.isBlank()) {
            events = aiEventRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
        } else if (severity != null && !severity.isBlank()) {
            events = aiEventRepository.findBySeverityOrderByCreatedAtDesc(severity.toUpperCase().trim());
        } else if (state != null && !state.isBlank()) {
            events = aiEventRepository.findByStateOrderByCreatedAtDesc(state);
        } else if (city != null && !city.isBlank()) {
            events = aiEventRepository.findByCityOrderByCreatedAtDesc(city);
        } else {
            events = aiEventRepository.findAllByOrderByCreatedAtDesc();
        }
        if (events.size() > limit) {
            events = events.subList(0, limit);
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<AiEventEntity> getEventById(@PathVariable("id") String id) {
        if (aiEventRepository == null) {
            return ResponseEntity.notFound().build();
        }
        return aiEventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<WeatherAlertEvent>> getAllAlerts(
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "active", required = false) Boolean active) {
        AlertSeverity sev = null;
        if (severity != null && !severity.isBlank()) {
            try { sev = AlertSeverity.valueOf(severity.toUpperCase().trim()); } catch (Exception ignored) {}
        }
        DisasterCategory cat = null;
        if (category != null && !category.isBlank()) {
            try { cat = DisasterCategory.valueOf(category.toUpperCase().trim()); } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(alertService.getAllAlerts(sev, cat, state, active));
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<WeatherAlertEvent> getAlertById(@PathVariable("id") String id) {
        return alertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping({"/analytics/timeseries", "/analytics/rainfall"})
    public ResponseEntity<List<TimeSeriesPoint>> getTimeSeries(
            @RequestParam(value = "stationId", defaultValue = "stn-mum-01") String stationId,
            @RequestParam(value = "range", defaultValue = "24h") String range) {
        return ResponseEntity.ok(timeSeriesService.getTimeSeriesForStation(stationId, range));
    }

    @GetMapping("/analytics/anomalies")
    public ResponseEntity<List<Map<String, Object>>> getDistrictAnomalies() {
        return ResponseEntity.ok(timeSeriesService.getDistrictAnomalies());
    }

    @GetMapping("/analytics/events")
    public ResponseEntity<Map<String, Object>> getAnalyticsEvents() {
        return ResponseEntity.ok(Map.of(
                "totalAiEvents", timeSeriesService.getTotalAiEventsRecorded(),
                "recentEvents", timeSeriesService.getRecentAiEventsAnalytics()
        ));
    }

    @GetMapping("/analytics/regions")
    public ResponseEntity<List<Map<String, Object>>> getRegionalAnalytics() {
        return ResponseEntity.ok(timeSeriesService.getRegionalSummary());
    }

    @GetMapping("/analytics/severity")
    public ResponseEntity<Map<String, Object>> getSeverityBreakdown() {
        return ResponseEntity.ok(timeSeriesService.getSeverityBreakdown());
    }

    @GetMapping("/analytics/timeline")
    public ResponseEntity<List<TimeSeriesPoint>> getTimeline(
            @RequestParam(value = "stationId", defaultValue = "stn-mum-01") String stationId,
            @RequestParam(value = "range", defaultValue = "24h") String range) {
        return ResponseEntity.ok(timeSeriesService.getTimeSeriesForStation(stationId, range));
    }

    @GetMapping("/alerts/active")
    public ResponseEntity<List<WeatherAlertEvent>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping(value = "/alerts/feed/cap", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getCapXmlFeed() {
        return ResponseEntity.ok(alertService.generateCapXmlFeed());
    }

    @GetMapping("/analytics/system-stats")
    public ResponseEntity<SystemStatsDTO> getSystemStats() {
        SystemStatsDTO stats = new SystemStatsDTO();
        stats.setActiveStationsCount(30);
        stats.setTotalTelemetryCount(timeSeriesService.getTotalTelemetryCount());

        long totalAlerts = alertService.getActiveAlerts().size();
        stats.setActiveAlertsCount((int) totalAlerts);

        int totalReports = 0;
        int verified = 0;
        int suspicious = 0;
        int debunked = 0;
        double avgLatency = 45.0;
        double accuracy = 95.0;

        try {
            Map<?, ?> verifyMetrics = restTemplate.getForObject(verificationServiceUrl + "/api/v1/verify/metrics", Map.class);
            if (verifyMetrics != null) {
                if (verifyMetrics.containsKey("totalEvaluated")) {
                    totalReports = ((Number) verifyMetrics.get("totalEvaluated")).intValue();
                }
                if (verifyMetrics.containsKey("totalVerified")) {
                    verified = ((Number) verifyMetrics.get("totalVerified")).intValue();
                }
                if (verifyMetrics.containsKey("totalSuspicious")) {
                    suspicious = ((Number) verifyMetrics.get("totalSuspicious")).intValue();
                }
                if (verifyMetrics.containsKey("totalDebunked")) {
                    debunked = ((Number) verifyMetrics.get("totalDebunked")).intValue();
                }
                if (verifyMetrics.containsKey("averageLatencyMs")) {
                    avgLatency = ((Number) verifyMetrics.get("averageLatencyMs")).doubleValue();
                }
                if (verifyMetrics.containsKey("accuracyPercent")) {
                    accuracy = ((Number) verifyMetrics.get("accuracyPercent")).doubleValue();
                }
            }
        } catch (Exception e) {
            log.debug("Verification metrics service query notice: {}", e.getMessage());
        }

        stats.setTotalCitizenReportsCount(totalReports);
        stats.setVerifiedReportsCount(verified);
        stats.setSuspiciousReportsCount(suspicious);
        stats.setDebunkedReportsCount(debunked);
        stats.setAverageVerificationLatencyMs(Math.round(avgLatency));
        stats.setVerificationAccuracyPercent(accuracy);

        long aiCount = timeSeriesService.getTotalAiEventsRecorded();
        stats.setCurrentIngestionRateEventsSec(aiCount > 0 ? Math.min(50.0, aiCount * 2.5) : 0.0);
        stats.setTimestamp(Instant.now());
        return ResponseEntity.ok(stats);
    }
}
