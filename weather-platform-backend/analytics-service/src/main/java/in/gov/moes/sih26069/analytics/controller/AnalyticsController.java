package in.gov.moes.sih26069.analytics.controller;

import in.gov.moes.sih26069.analytics.service.AlertManagementService;
import in.gov.moes.sih26069.analytics.service.ClickHouseTimeSeriesService;
import in.gov.moes.sih26069.common.dto.SystemStatsDTO;
import in.gov.moes.sih26069.common.dto.TimeSeriesPoint;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private ClickHouseTimeSeriesService timeSeriesService;

    @Autowired
    private AlertManagementService alertService;

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
        stats.setTotalCitizenReportsCount(14);
        stats.setVerifiedReportsCount(12);
        stats.setSuspiciousReportsCount(1);
        stats.setDebunkedReportsCount(1);
        stats.setVerificationAccuracyPercent(92.8);
        stats.setCurrentIngestionRateEventsSec(25.0);
        stats.setAverageVerificationLatencyMs(48);
        stats.setActiveAlertsCount(alertService.getActiveAlerts().size());
        stats.setTimestamp(Instant.now());
        return ResponseEntity.ok(stats);
    }
}
