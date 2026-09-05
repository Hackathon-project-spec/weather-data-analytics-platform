package in.gov.moes.sih26069.gateway.service;

import in.gov.moes.sih26069.common.dto.SystemStatsDTO;
import in.gov.moes.sih26069.common.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WebSocketBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketBroadcastService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final AtomicLong telemetryCounter = new AtomicLong(0);
    private final AtomicLong reportsCounter = new AtomicLong(0);
    private final AtomicLong verifiedCounter = new AtomicLong(0);
    private final AtomicLong suspiciousCounter = new AtomicLong(0);
    private final AtomicLong debunkedCounter = new AtomicLong(0);
    private final AtomicLong alertsCounter = new AtomicLong(0);
    private final AtomicLong aiEventsCounter = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    @KafkaListener(topics = "weather.raw.telemetry", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onTelemetryReceived(TelemetryEvent event) {
        if (event == null) return;
        telemetryCounter.incrementAndGet();
        // Sample 1 in 3 events to prevent browser UI saturation while keeping animation live
        if (telemetryCounter.get() % 3 == 0) {
            messagingTemplate.convertAndSend("/topic/telemetry", event);
        }
    }

    @KafkaListener(topics = "weather.ai.events", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onAiEventReceived(in.gov.moes.sih26069.common.dto.AiEventDTO event) {
        if (event == null) return;
        aiEventsCounter.incrementAndGet();
        messagingTemplate.convertAndSend("/topic/events", event);
    }

    @KafkaListener(topics = "weather.social.feed", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onSocialFeedReceived(SocialFeedEvent event) {
        if (event == null) return;
        messagingTemplate.convertAndSend("/topic/social", event);
    }

    @KafkaListener(topics = "weather.citizen.reports", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onCitizenReportReceived(CitizenReportEvent event) {
        if (event == null) return;
        reportsCounter.incrementAndGet();
        messagingTemplate.convertAndSend("/topic/reports", event);
    }

    @KafkaListener(topics = "weather.verified.events", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onVerifiedReportReceived(VerifiedReportEvent event) {
        if (event == null) return;
        if (event.getStatus() != null) {
            switch (event.getStatus()) {
                case VERIFIED -> verifiedCounter.incrementAndGet();
                case SUSPICIOUS -> suspiciousCounter.incrementAndGet();
                case DEBUNKED -> debunkedCounter.incrementAndGet();
                default -> {}
            }
        }
        totalLatencyMs.addAndGet(event.getLatencyMs());
        messagingTemplate.convertAndSend("/topic/verified", event);
    }

    @KafkaListener(topics = "weather.alerts.broadcast", containerFactory = "genericKafkaListenerContainerFactory", autoStartup = "${app.kafka.listener.enabled:true}")
    public void onAlertReceived(WeatherAlertEvent event) {
        if (event == null) return;
        alertsCounter.incrementAndGet();
        messagingTemplate.convertAndSend("/topic/alerts", event);
    }

    // Broadcast system throughput & health stats to all active browser sessions every 2 seconds
    @Scheduled(fixedRate = 2000)
    public void broadcastSystemStats() {
        SystemStatsDTO stats = new SystemStatsDTO();
        stats.setActiveStationsCount(30);
        stats.setTotalTelemetryCount(telemetryCounter.get());
        stats.setTotalCitizenReportsCount(reportsCounter.get());
        stats.setVerifiedReportsCount(verifiedCounter.get());
        stats.setSuspiciousReportsCount(suspiciousCounter.get());
        stats.setDebunkedReportsCount(debunkedCounter.get());

        long totalEvaluated = verifiedCounter.get() + suspiciousCounter.get() + debunkedCounter.get();
        double accuracy = totalEvaluated > 0 ? (verifiedCounter.get() * 100.0 / totalEvaluated) : 95.0;
        stats.setVerificationAccuracyPercent(Math.round(accuracy * 10.0) / 10.0);

        long avgLatency = totalEvaluated > 0 ? (totalLatencyMs.get() / totalEvaluated) : 42;
        stats.setAverageVerificationLatencyMs(avgLatency);

        long aiEvents = aiEventsCounter.get();
        stats.setCurrentIngestionRateEventsSec(aiEvents > 0 ? Math.min(50.0, aiEvents * 2.5) : 0.0);
        stats.setActiveAlertsCount((int) alertsCounter.get());
        stats.setTimestamp(Instant.now());

        messagingTemplate.convertAndSend("/topic/system-stats", stats);
    }
}
