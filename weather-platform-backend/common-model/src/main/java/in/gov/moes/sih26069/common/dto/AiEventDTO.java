package in.gov.moes.sih26069.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AiEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "eventId is required")
    private String eventId;

    @NotBlank(message = "eventType is required")
    private String eventType;

    private String source = "AI_ANALYSIS";

    @NotNull(message = "location is required")
    @Valid
    private GeoLocation location;

    @NotBlank(message = "severity is required")
    private String severity;

    @NotNull(message = "confidence is required")
    @DecimalMin(value = "0.0", message = "confidence must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "confidence must be between 0 and 100")
    private Double confidence;

    @Min(value = 0, message = "reportCount cannot be negative")
    private int reportCount;

    private String summary;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant observedAt = Instant.now();

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant processedAt = Instant.now();

    private Map<String, Object> metadata = new HashMap<>();

    private OperationalEventStatus operationalStatus;

    public AiEventDTO() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public OperationalEventStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(OperationalEventStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    @Override
    public String toString() {
        return "AiEventDTO{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", location=" + location +
                ", severity='" + severity + '\'' +
                ", confidence=" + confidence +
                ", reportCount=" + reportCount +
                ", summary='" + summary + '\'' +
                ", operationalStatus=" + operationalStatus +
                '}';
    }
}
