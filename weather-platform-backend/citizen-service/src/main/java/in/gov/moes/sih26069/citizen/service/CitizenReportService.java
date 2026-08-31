package in.gov.moes.sih26069.citizen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.moes.sih26069.citizen.entity.CitizenReportEntity;
import in.gov.moes.sih26069.citizen.repository.CitizenReportRepository;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import in.gov.moes.sih26069.common.event.CitizenReportEvent;
import in.gov.moes.sih26069.common.event.VerifiedReportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CitizenReportService {

    private static final Logger log = LoggerFactory.getLogger(CitizenReportService.class);

    @Autowired
    private CitizenReportRepository repository;

    @Autowired(required = false)
    private KafkaTemplate<String, CitizenReportEvent> kafkaTemplate;

    @Value("${services.verification.url:http://localhost:8083}")
    private String verificationEngineUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public CitizenReportEntity submitReport(CitizenReportEvent event) {
        if (event.getReportId() == null || event.getReportId().isBlank()) {
            event.setReportId("rep-" + UUID.randomUUID().toString().substring(0, 10));
        }

        CitizenReportEntity entity = new CitizenReportEntity();
        entity.setId(event.getReportId());
        entity.setReporterName(event.getReporterName() != null ? event.getReporterName() : "Anonymous Citizen");
        entity.setReporterContactHash(event.getReporterContactHash());
        entity.setCategory(event.getCategory());
        entity.setSeverityLevel(event.getSeverityLevel() > 0 ? event.getSeverityLevel() : 3);
        entity.setLatitude(event.getLatitude());
        entity.setLongitude(event.getLongitude());
        entity.setState(event.getState());
        entity.setDistrict(event.getDistrict());
        entity.setDescription(event.getDescription());
        entity.setMediaUrl(event.getMediaUrl());
        entity.setVerificationStatus(VerificationStatus.PENDING);
        entity.setConfidenceScore(0.0);
        entity.setUpvotes(0);
        entity.setCreatedAt(Instant.now());

        entity = repository.save(entity);
        log.info("Citizen report stored in PostgreSQL: ID={} Category={} District={}", entity.getId(), entity.getCategory(), entity.getDistrict());

        // 1. Publish to Kafka event backbone
        boolean kafkaSent = false;
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("weather.citizen.reports", entity.getId(), event);
                log.info("Citizen report published to Kafka topic weather.citizen.reports: ID={}", entity.getId());
                kafkaSent = true;
            } catch (Exception e) {
                log.warn("Kafka send error for report {}: {}", entity.getId(), e.getMessage());
            }
        }

        // 2. Direct asynchronous verification pipeline fallback
        try {
            VerifiedReportEvent verified = restTemplate.postForObject(
                verificationEngineUrl + "/api/v1/verify/evaluate", event, VerifiedReportEvent.class
            );
            if (verified != null) {
                onVerifiedReportReceived(verified);
                entity = repository.findById(entity.getId()).orElse(entity);
            }
        } catch (Exception e) {
            log.debug("Verification engine sync check: {}", e.getMessage());
        }

        return entity;
    }

    public List<CitizenReportEntity> getAllReports(String status, String state) {
        if (status != null && !status.isBlank()) {
            try {
                VerificationStatus vStatus = VerificationStatus.valueOf(status.toUpperCase());
                return repository.findByVerificationStatusOrderByCreatedAtDesc(vStatus);
            } catch (IllegalArgumentException ignored) {}
        }
        if (state != null && !state.isBlank()) {
            return repository.findByStateOrderByCreatedAtDesc(state);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<CitizenReportEntity> getReportById(String id) {
        return repository.findById(id);
    }

    @Transactional
    public Optional<CitizenReportEntity> upvoteReport(String id) {
        return repository.findById(id).map(r -> {
            r.setUpvotes(r.getUpvotes() + 1);
            return repository.save(r);
        });
    }

    @KafkaListener(topics = "weather.verified.events", containerFactory = "verifiedReportListenerFactory", autoStartup = "${app.kafka.listener.enabled:false}")
    @Transactional
    public void onVerifiedReportReceived(VerifiedReportEvent verifiedEvent) {
        if (verifiedEvent == null || verifiedEvent.getReportId() == null) return;
        log.info("Received verification result: ID={} Status={} Score={}% Latency={}ms",
            verifiedEvent.getReportId(), verifiedEvent.getStatus(), verifiedEvent.getConfidenceScore(), verifiedEvent.getLatencyMs());

        repository.findById(verifiedEvent.getReportId()).ifPresent(entity -> {
            entity.setVerificationStatus(verifiedEvent.getStatus());
            entity.setConfidenceScore(verifiedEvent.getConfidenceScore());
            entity.setVerificationReasoning(verifiedEvent.getReasoning());
            entity.setMatchedStationId(verifiedEvent.getMatchedStationId());
            entity.setStationDistanceKm(verifiedEvent.getStationDistanceKm());
            entity.setVerificationLatencyMs(verifiedEvent.getLatencyMs());
            entity.setVerifiedAt(verifiedEvent.getVerifiedAt() != null ? verifiedEvent.getVerifiedAt() : Instant.now());
            try {
                if (verifiedEvent.getScoreBreakdown() != null) {
                    entity.setScoreBreakdown(objectMapper.writeValueAsString(verifiedEvent.getScoreBreakdown()));
                }
            } catch (Exception ignored) {}
            repository.save(entity);
        });
    }
}
