package in.gov.moes.sih26069.ingestion.service;

import in.gov.moes.sih26069.common.dto.AiEventDTO;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiEventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(AiEventIngestionService.class);
    public static final String TOPIC_WEATHER_AI_EVENTS = "weather.ai.events";

    @Autowired(required = false)
    private KafkaTemplate<String, AiEventDTO> aiEventKafkaTemplate;

    // In-memory sliding cache to detect duplicate events by eventId
    // In production cluster with Redis, this keys off Redis SETNX with TTL
    private final Map<String, Instant> processedEventIds = new ConcurrentHashMap<>();

    public Map<String, Object> ingestAiEvent(AiEventDTO event) {
        String correlationId = UUID.randomUUID().toString();
        String eventId = event.getEventId();

        log.info("[INFO] eventId={} correlationId={} service=ingestion-service Received AI event {} for city={} state={} confidence={}%",
                eventId, correlationId, event.getEventType(),
                event.getLocation() != null ? event.getLocation().getCity() : "Unknown",
                event.getLocation() != null ? event.getLocation().getState() : "Unknown",
                event.getConfidence());

        // 1. Idempotency / Duplicate Detection
        if (processedEventIds.containsKey(eventId)) {
            log.warn("[WARN] eventId={} correlationId={} service=ingestion-service Duplicate event received. Skipping re-publication to Kafka.",
                    eventId, correlationId);
            return Map.of(
                    "status", "DUPLICATE_ACCEPTED",
                    "message", "Event with eventId '" + eventId + "' was already received and processed.",
                    "eventId", eventId,
                    "correlationId", correlationId
            );
        }

        // 2. Normalization
        if (event.getObservedAt() == null) {
            event.setObservedAt(Instant.now());
        }
        event.setProcessedAt(Instant.now());

        if (event.getSource() == null || event.getSource().isBlank()) {
            event.setSource("AI_ANALYSIS");
        }

        // Normalize severity to uppercase
        if (event.getSeverity() != null) {
            event.setSeverity(event.getSeverity().toUpperCase().trim());
        }

        // Initial operational status
        event.setOperationalStatus(OperationalEventStatus.MONITORING);

        // Record into sliding window cache (clean old entries if map grows beyond 50,000)
        if (processedEventIds.size() > 50000) {
            processedEventIds.clear();
        }
        processedEventIds.put(eventId, Instant.now());

        // 3. Publish to Kafka Backbone
        boolean kafkaPublished = false;
        if (aiEventKafkaTemplate != null) {
            try {
                aiEventKafkaTemplate.send(TOPIC_WEATHER_AI_EVENTS, eventId, event);
                log.info("[INFO] eventId={} correlationId={} service=ingestion-service Published to {}",
                        eventId, correlationId, TOPIC_WEATHER_AI_EVENTS);
                kafkaPublished = true;
            } catch (Exception e) {
                log.error("[ERROR] eventId={} correlationId={} service=ingestion-service Failed to publish to Kafka: {}",
                        eventId, correlationId, e.getMessage());
            }
        } else {
            log.warn("[WARN] eventId={} correlationId={} service=ingestion-service KafkaTemplate not available (standalone mode)",
                    eventId, correlationId);
        }

        return Map.of(
                "status", "INGESTED",
                "eventId", eventId,
                "kafkaPublished", kafkaPublished,
                "correlationId", correlationId,
                "timestamp", Instant.now().toString()
        );
    }
}
