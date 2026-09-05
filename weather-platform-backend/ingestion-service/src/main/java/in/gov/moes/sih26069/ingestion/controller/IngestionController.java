package in.gov.moes.sih26069.ingestion.controller;

import in.gov.moes.sih26069.common.dto.ScenarioTriggerRequest;
import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.event.SocialFeedEvent;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import in.gov.moes.sih26069.ingestion.service.ScenarioLabService;
import in.gov.moes.sih26069.ingestion.service.StationCatalogService;
import in.gov.moes.sih26069.ingestion.service.TelemetrySimulatorEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class IngestionController {

    @Autowired
    private StationCatalogService catalogService;

    @Autowired
    private TelemetrySimulatorEngine simulatorEngine;

    @Autowired
    private ScenarioLabService scenarioLabService;

    @Autowired
    private in.gov.moes.sih26069.ingestion.service.AiEventIngestionService aiEventIngestionService;

    @Autowired(required = false)
    private KafkaTemplate<String, TelemetryEvent> telemetryKafkaTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, SocialFeedEvent> socialKafkaTemplate;

    @PostMapping({"/events/ai", "/ingestion/events", "/ingest/events"})
    public ResponseEntity<Map<String, Object>> ingestAiEvent(@jakarta.validation.Valid @RequestBody in.gov.moes.sih26069.common.dto.AiEventDTO event) {
        Map<String, Object> result = aiEventIngestionService.ingestAiEvent(event);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stations")
    public ResponseEntity<List<StationDTO>> getAllStations() {
        return ResponseEntity.ok(catalogService.getAllStations());
    }

    @PostMapping("/ingest/telemetry")
    public ResponseEntity<Map<String, Object>> ingestTelemetry(@RequestBody TelemetryEvent event) {
        catalogService.updateStationMetrics(event);
        if (telemetryKafkaTemplate != null) {
            telemetryKafkaTemplate.send("weather.raw.telemetry", event.getStationId(), event);
        }
        return ResponseEntity.ok(Map.of("status", "INGESTED", "stationId", event.getStationId()));
    }

    @PostMapping("/ingest/social")
    public ResponseEntity<Map<String, Object>> ingestSocial(@RequestBody SocialFeedEvent event) {
        if (socialKafkaTemplate != null) {
            socialKafkaTemplate.send("weather.social.feed", event.getPostId(), event);
        }
        return ResponseEntity.ok(Map.of("status", "INGESTED", "postId", event.getPostId()));
    }

    @PostMapping("/simulator/start")
    public ResponseEntity<Map<String, Object>> startSimulator(@RequestParam(value = "rate", defaultValue = "20") int rate) {
        simulatorEngine.setTargetRate(rate);
        simulatorEngine.setRunning(true);
        return ResponseEntity.ok(Map.of("status", "RUNNING", "rate", simulatorEngine.getTargetRate()));
    }

    @PostMapping("/simulator/stop")
    public ResponseEntity<Map<String, Object>> stopSimulator() {
        simulatorEngine.setRunning(false);
        return ResponseEntity.ok(Map.of("status", "STOPPED"));
    }

    @PostMapping("/simulator/rate")
    public ResponseEntity<Map<String, Object>> updateRate(@RequestParam(value = "rate") int rate) {
        simulatorEngine.setTargetRate(rate);
        return ResponseEntity.ok(Map.of("status", "UPDATED", "rate", simulatorEngine.getTargetRate()));
    }

    @GetMapping("/simulator/status")
    public ResponseEntity<Map<String, Object>> getSimulatorStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", simulatorEngine.isRunning());
        status.put("targetRateEventsPerSec", simulatorEngine.getTargetRate());
        status.put("totalEventsGenerated", simulatorEngine.getTotalEventsGenerated());
        status.put("activeScenario", scenarioLabService.getActiveScenarioStatus());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/simulator/trigger-scenario")
    public ResponseEntity<Map<String, Object>> triggerScenario(@RequestBody ScenarioTriggerRequest request) {
        Map<String, Object> result = scenarioLabService.triggerScenario(request);
        return ResponseEntity.ok(result);
    }
}
