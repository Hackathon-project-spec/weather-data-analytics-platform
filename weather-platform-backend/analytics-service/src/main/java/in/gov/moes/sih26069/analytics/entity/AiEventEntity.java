package in.gov.moes.sih26069.analytics.entity;

import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_events")
public class AiEventEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(name = "source", length = 64, nullable = false)
    private String source = "AI_ANALYSIS";

    @Column(name = "city", length = 128)
    private String city;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "report_count")
    private int reportCount = 0;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", length = 32, nullable = false)
    private OperationalEventStatus operationalStatus = OperationalEventStatus.MONITORING;

    @Column(name = "observed_at")
    private Instant observedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public AiEventEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public OperationalEventStatus getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(OperationalEventStatus operationalStatus) { this.operationalStatus = operationalStatus; }

    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
