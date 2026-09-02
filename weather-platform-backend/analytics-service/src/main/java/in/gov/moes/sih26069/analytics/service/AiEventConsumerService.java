package in.gov.moes.sih26069.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.moes.sih26069.analytics.entity.AiEventEntity;
import in.gov.moes.sih26069.analytics.entity.WeatherAlertEntity;
import in.gov.moes.sih26069.analytics.repository.AiEventRepository;
import in.gov.moes.sih26069.analytics.repository.WeatherAlertRepository;
import in.gov.moes.sih26069.common.dto.AiEventDTO;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiEventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(AiEventConsumerService.class);
    public static final String TOPIC_WEATHER_ALERTS_BROADCAST = "weather.alerts.broadcast";

    @Autowired(required = false)
    private AiEventRepository aiEventRepository;

    @Autowired(required = false)
    private WeatherAlertRepository alertRepository;

    @Autowired(required = false)
    private AlertManagementService alertManagementService;

    @Autowired(required = false)
    private ClickHouseTimeSeriesService clickHouseService;

    @Autowired(required = false)
    private KafkaTemplate<String, WeatherAlertEvent> alertKafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> processedEventIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @KafkaListener(topics = "weather.ai.events", containerFactory = "aiEventListenerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    @Transactional
    public void onAiEventReceived(AiEventDTO event) {
        if (event == null || event.getEventId() == null) return;
        String eventId = event.getEventId();

        // 1. Idempotency Check
        if (!processedEventIds.add(eventId)) {
            log.info("[INFO] eventId={} service=analytics-service Duplicate AI event in stream, skipping.", eventId);
            return;
        }

        if (aiEventRepository != null && aiEventRepository.existsById(eventId)) {
            log.info("[INFO] eventId={} service=analytics-service Event already persisted in PostgreSQL, skipping duplicate.", eventId);
            return;
        }

        log.info("[INFO] eventId={} service=analytics-service Consumed AI event {} confidence={}% severity={}",
                eventId, event.getEventType(), event.getConfidence(), event.getSeverity());

        // 2. Operational Decision Evaluation
        boolean isHighConfidence = event.getConfidence() != null && event.getConfidence() >= 75.0;
        String normSeverity = event.getSeverity() != null ? event.getSeverity().toUpperCase().trim() : "MODERATE";
        boolean isHighSeverity = normSeverity.equals("HIGH") || normSeverity.equals("EXTREME") || normSeverity.equals("SEVERE");

        OperationalEventStatus operationalStatus;
        if (isHighConfidence && isHighSeverity) {
            operationalStatus = OperationalEventStatus.ACTIVE_ALERT;
        } else if (event.getConfidence() != null && event.getConfidence() < 30.0) {
            operationalStatus = OperationalEventStatus.DEBUNKED;
        } else {
            operationalStatus = OperationalEventStatus.MONITORING;
        }

        event.setOperationalStatus(operationalStatus);

        // 3. Persist Event to PostgreSQL
        AiEventEntity entity = new AiEventEntity();
        entity.setId(eventId);
        entity.setEventType(event.getEventType());
        entity.setSource(event.getSource() != null ? event.getSource() : "AI_ANALYSIS");
        if (event.getLocation() != null) {
            entity.setCity(event.getLocation().getCity());
            entity.setState(event.getLocation().getState());
            entity.setLatitude(event.getLocation().getLatitude() != null ? event.getLocation().getLatitude() : 0.0);
            entity.setLongitude(event.getLocation().getLongitude() != null ? event.getLocation().getLongitude() : 0.0);
        }
        entity.setSeverity(normSeverity);
        entity.setConfidence(event.getConfidence() != null ? event.getConfidence() : 0.0);
        entity.setReportCount(event.getReportCount());
        entity.setSummary(event.getSummary());
        entity.setOperationalStatus(operationalStatus);
        entity.setObservedAt(event.getObservedAt() != null ? event.getObservedAt() : Instant.now());
        entity.setProcessedAt(Instant.now());
        try {
            if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
                entity.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
            }
        } catch (Exception ignored) {}

        if (aiEventRepository != null) {
            try {
                aiEventRepository.save(entity);
                log.info("[INFO] eventId={} service=analytics-service Stored in PostgreSQL ai_events with status={}", eventId, operationalStatus);
            } catch (Exception e) {
                log.warn("[WARN] eventId={} PostgreSQL save fallback: {}", eventId, e.getMessage());
            }
        }

        // 4. Record to ClickHouse Time-Series Analytics
        if (clickHouseService != null) {
            clickHouseService.recordAiEventAnalytics(event, operationalStatus);
        }

        // 5. Active Alert Broadcast if OPERATIONAL DECISION triggers ACTIVE_ALERT
        if (operationalStatus == OperationalEventStatus.ACTIVE_ALERT) {
            WeatherAlertEvent alertEvent = createAlertFromAiEvent(event);

            // Persist Alert to PostgreSQL
            if (alertRepository != null) {
                WeatherAlertEntity alertEntity = new WeatherAlertEntity();
                alertEntity.setId(alertEvent.getAlertId());
                alertEntity.setIdentifier(alertEvent.getIdentifier());
                alertEntity.setHeadline(alertEvent.getHeadline());
                alertEntity.setDescription(alertEvent.getDescription());
                alertEntity.setInstruction(alertEvent.getInstruction());
                alertEntity.setSeverity(alertEvent.getSeverity());
                alertEntity.setEventCategory(alertEvent.getCategory());
                alertEntity.setAffectedState(alertEvent.getAffectedState());
                alertEntity.setAffectedDistrict(alertEvent.getAffectedDistrict());
                alertEntity.setCenterLat(alertEvent.getCenterLat());
                alertEntity.setCenterLon(alertEvent.getCenterLon());
                alertEntity.setRadiusKm(alertEvent.getRadiusKm());
                alertEntity.setEffectiveFrom(Instant.now());
                alertEntity.setExpiresAt(Instant.now().plus(6, ChronoUnit.HOURS));
                alertEntity.setIsActive(true);
                try {
                    alertRepository.save(alertEntity);
                    log.info("[INFO] eventId={} service=analytics-service Alert stored in PostgreSQL weather_alerts: ID={}", eventId, alertEvent.getAlertId());
                } catch (Exception e) {
                    log.warn("[WARN] eventId={} Alert PostgreSQL save warning: {}", eventId, e.getMessage());
                }
            }

            // Update Alert Management Cache (Redis & in-memory)
            if (alertManagementService != null) {
                alertManagementService.cacheActiveAlert(alertEvent);
            }

            // Publish to Kafka weather.alerts.broadcast
            if (alertKafkaTemplate != null) {
                try {
                    alertKafkaTemplate.send(TOPIC_WEATHER_ALERTS_BROADCAST, alertEvent.getAlertId(), alertEvent);
                    log.info("[INFO] eventId={} service=analytics-service Published alert to {}", eventId, TOPIC_WEATHER_ALERTS_BROADCAST);
                } catch (Exception e) {
                    log.error("[ERROR] eventId={} Failed to publish alert to Kafka: {}", eventId, e.getMessage());
                }
            }
        } else {
            log.info("[INFO] eventId={} service=analytics-service Low-confidence/monitoring event ({}%) stored for analysis without public alert broadcast",
                    eventId, event.getConfidence());
        }
    }

    private WeatherAlertEvent createAlertFromAiEvent(AiEventDTO event) {
        WeatherAlertEvent alert = new WeatherAlertEvent();
        String alertId = "alt-ai-" + event.getEventId();
        alert.setAlertId(alertId);
        alert.setIdentifier("MOES-AI-" + event.getEventType() + "-" + event.getEventId().toUpperCase());

        DisasterCategory cat;
        try {
            cat = DisasterCategory.valueOf(event.getEventType().toUpperCase().trim());
        } catch (Exception e) {
            cat = DisasterCategory.FLOOD;
        }
        alert.setCategory(cat);

        AlertSeverity sev;
        try {
            sev = AlertSeverity.valueOf(event.getSeverity().toUpperCase().trim());
        } catch (Exception e) {
            sev = AlertSeverity.SEVERE;
        }
        alert.setSeverity(sev);

        String city = event.getLocation() != null && event.getLocation().getCity() != null ? event.getLocation().getCity() : "Target Area";
        String state = event.getLocation() != null && event.getLocation().getState() != null ? event.getLocation().getState() : "India";

        alert.setHeadline(String.format("HIGH-PRIORITY ALERT: Severe %s reported in %s", event.getEventType(), city));
        alert.setDescription(event.getSummary() != null ? event.getSummary() : String.format("Automated operational detection confirms high-confidence %s in %s.", event.getEventType(), city));
        alert.setInstruction(String.format("Residents of %s, %s should monitor local advisories and remain alert. Helpline 1070.", city, state));
        alert.setAffectedDistrict(city);
        alert.setAffectedState(state);
        alert.setCenterLat(event.getLocation() != null ? event.getLocation().getLatitude() : 0.0);
        alert.setCenterLon(event.getLocation() != null ? event.getLocation().getLongitude() : 0.0);
        alert.setRadiusKm(25.0);
        alert.setActive(true);

        return alert;
    }
}
