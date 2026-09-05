package in.gov.moes.sih26069.analytics.service;

import in.gov.moes.sih26069.analytics.entity.WeatherAlertEntity;
import in.gov.moes.sih26069.analytics.repository.WeatherAlertRepository;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertManagementService {

    private static final Logger log = LoggerFactory.getLogger(AlertManagementService.class);
    private static final String REDIS_ALERTS_KEY = "weather:alerts:active";

    @Autowired(required = false)
    private WeatherAlertRepository alertRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, WeatherAlertEvent> activeAlerts = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadActiveAlertsFromPostgres();
    }

    public void loadActiveAlertsFromPostgres() {
        if (alertRepository == null) return;
        try {
            List<WeatherAlertEntity> dbAlerts = alertRepository.findByIsActiveTrueAndExpiresAtAfterOrderBySentAtDesc(Instant.now());
            activeAlerts.clear();
            for (WeatherAlertEntity e : dbAlerts) {
                WeatherAlertEvent event = entityToEvent(e);
                activeAlerts.put(event.getAlertId(), event);
                cacheActiveAlert(event);
            }
            log.info("Loaded and cached {} active alerts from PostgreSQL weather_alerts", dbAlerts.size());
        } catch (Exception e) {
            log.debug("PostgreSQL alerts init check notice: {}", e.getMessage());
        }
    }

    public void cacheActiveAlert(WeatherAlertEvent alert) {
        if (alert == null || alert.getAlertId() == null) return;
        activeAlerts.put(alert.getAlertId(), alert);

        // Update Redis Cache
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForHash().put(REDIS_ALERTS_KEY, alert.getAlertId(), alert);
                redisTemplate.expire(REDIS_ALERTS_KEY, Duration.ofHours(6));
                log.debug("Active alert cached in Redis key {}: ID={}", REDIS_ALERTS_KEY, alert.getAlertId());
            } catch (Exception e) {
                log.debug("Redis cache write notice: {}", e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "weather.alerts.broadcast", containerFactory = "alertListenerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onAlertReceived(WeatherAlertEvent alert) {
        if (alert == null || alert.getAlertId() == null) return;
        log.info("[INFO] alertId={} service=analytics-service Received active weather alert from Kafka: Headline={}",
                alert.getAlertId(), alert.getHeadline());
        cacheActiveAlert(alert);
    }

    public List<WeatherAlertEvent> getActiveAlerts() {
        // 1. Try reading from Redis first if available
        if (redisTemplate != null) {
            try {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_ALERTS_KEY);
                if (entries != null && !entries.isEmpty()) {
                    List<WeatherAlertEvent> cached = new ArrayList<>();
                    for (Object val : entries.values()) {
                        if (val instanceof WeatherAlertEvent alert) {
                            cached.add(alert);
                        }
                    }
                    if (!cached.isEmpty()) {
                        return cached;
                    }
                }
            } catch (Exception e) {
                log.debug("Redis cache read notice (falling back to database/memory): {}", e.getMessage());
            }
        }

        // 2. Query PostgreSQL repository for active non-expired alerts
        if (alertRepository != null) {
            try {
                List<WeatherAlertEntity> dbAlerts = alertRepository.findByIsActiveTrueAndExpiresAtAfterOrderBySentAtDesc(Instant.now());
                if (!dbAlerts.isEmpty()) {
                    List<WeatherAlertEvent> list = new ArrayList<>();
                    for (WeatherAlertEntity entity : dbAlerts) {
                        WeatherAlertEvent event = entityToEvent(entity);
                        list.add(event);
                        activeAlerts.put(event.getAlertId(), event);
                    }
                    return list;
                }
            } catch (Exception e) {
                log.debug("PostgreSQL active alerts query notice: {}", e.getMessage());
            }
        }

        return new ArrayList<>(activeAlerts.values());
    }

    public List<WeatherAlertEvent> getAllAlerts(AlertSeverity severity, DisasterCategory category, String state, Boolean active) {
        if (alertRepository != null) {
            try {
                List<WeatherAlertEntity> list;
                if (active != null) {
                    list = alertRepository.findByIsActiveOrderBySentAtDesc(active);
                } else if (severity != null) {
                    list = alertRepository.findBySeverityOrderBySentAtDesc(severity);
                } else if (category != null) {
                    list = alertRepository.findByEventCategoryOrderBySentAtDesc(category);
                } else if (state != null && !state.isBlank()) {
                    list = alertRepository.findByAffectedStateOrderBySentAtDesc(state);
                } else {
                    list = alertRepository.findAllByOrderBySentAtDesc();
                }
                return list.stream().map(this::entityToEvent).toList();
            } catch (Exception e) {
                log.warn("Error fetching alerts from repository: {}", e.getMessage());
            }
        }
        return new ArrayList<>(activeAlerts.values());
    }

    public Optional<WeatherAlertEvent> getAlertById(String id) {
        if (alertRepository != null) {
            try {
                return alertRepository.findById(id).map(this::entityToEvent);
            } catch (Exception e) {
                log.warn("Error finding alert by ID {}: {}", id, e.getMessage());
            }
        }
        return Optional.ofNullable(activeAlerts.get(id));
    }

    public String generateCapXmlFeed() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<alert xmlns=\"urn:oasis:names:tc:emergency:cap:1.2\">\n");
        xml.append("  <identifier>MOES-CAP-FEED-").append(System.currentTimeMillis()).append("</identifier>\n");
        xml.append("  <sender>in.gov.moes.weather-platform</sender>\n");
        xml.append("  <sent>").append(Instant.now().toString()).append("</sent>\n");
        xml.append("  <status>Actual</status>\n");
        xml.append("  <msgType>Alert</msgType>\n");
        xml.append("  <scope>Public</scope>\n");
        for (WeatherAlertEvent a : getActiveAlerts()) {
            xml.append("  <info>\n");
            xml.append("    <category>Met</category>\n");
            xml.append("    <event>").append(a.getCategory()).append("</event>\n");
            xml.append("    <urgency>Immediate</urgency>\n");
            xml.append("    <severity>").append(a.getSeverity()).append("</severity>\n");
            xml.append("    <headline>").append(escapeXml(a.getHeadline())).append("</headline>\n");
            xml.append("    <description>").append(escapeXml(a.getDescription())).append("</description>\n");
            xml.append("    <instruction>").append(escapeXml(a.getInstruction())).append("</instruction>\n");
            xml.append("    <area>\n");
            xml.append("      <areaDesc>").append(a.getAffectedDistrict()).append(", ").append(a.getAffectedState()).append("</areaDesc>\n");
            xml.append("      <circle>").append(a.getCenterLat()).append(",").append(a.getCenterLon()).append(" ").append(a.getRadiusKm()).append("</circle>\n");
            xml.append("    </area>\n");
            xml.append("  </info>\n");
        }
        xml.append("</alert>");
        return xml.toString();
    }

    private WeatherAlertEvent entityToEvent(WeatherAlertEntity e) {
        WeatherAlertEvent event = new WeatherAlertEvent();
        event.setAlertId(e.getId());
        event.setIdentifier(e.getIdentifier());
        event.setSeverity(e.getSeverity());
        event.setCategory(e.getEventCategory());
        event.setHeadline(e.getHeadline());
        event.setDescription(e.getDescription());
        event.setInstruction(e.getInstruction());
        event.setAffectedState(e.getAffectedState());
        event.setAffectedDistrict(e.getAffectedDistrict());
        event.setCenterLat(e.getCenterLat());
        event.setCenterLon(e.getCenterLon());
        event.setRadiusKm(e.getRadiusKm());
        event.setActive(e.getIsActive());
        return event;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
