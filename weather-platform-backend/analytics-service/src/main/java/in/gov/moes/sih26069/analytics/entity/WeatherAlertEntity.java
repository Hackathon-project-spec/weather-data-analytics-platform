package in.gov.moes.sih26069.analytics.entity;

import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "weather_alerts")
public class WeatherAlertEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 128, nullable = false, unique = true)
    private String identifier;

    @Column(length = 128, nullable = false)
    private String sender = "MoES-IMD-Analytics-DSS";

    @Column(name = "sent_at")
    private Instant sentAt = Instant.now();

    @Column(length = 32, nullable = false)
    private String status = "Actual";

    @Column(name = "msg_type", length = 32, nullable = false)
    private String msgType = "Alert";

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private AlertSeverity severity;

    @Column(length = 32, nullable = false)
    private String urgency = "Immediate";

    @Column(length = 32, nullable = false)
    private String certainty = "Observed";

    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", length = 64, nullable = false)
    private DisasterCategory eventCategory;

    @Column(length = 256, nullable = false)
    private String headline;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "affected_state", length = 64, nullable = false)
    private String affectedState;

    @Column(name = "affected_district", length = 64, nullable = false)
    private String affectedDistrict;

    @Column(name = "radius_km")
    private Double radiusKm;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lon")
    private Double centerLon;

    @Column(name = "effective_from")
    private Instant effectiveFrom = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public WeatherAlertEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getCertainty() { return certainty; }
    public void setCertainty(String certainty) { this.certainty = certainty; }

    public DisasterCategory getEventCategory() { return eventCategory; }
    public void setEventCategory(DisasterCategory eventCategory) { this.eventCategory = eventCategory; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public String getAffectedState() { return affectedState; }
    public void setAffectedState(String affectedState) { this.affectedState = affectedState; }

    public String getAffectedDistrict() { return affectedDistrict; }
    public void setAffectedDistrict(String affectedDistrict) { this.affectedDistrict = affectedDistrict; }

    public Double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(Double radiusKm) { this.radiusKm = radiusKm; }

    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }

    public Double getCenterLon() { return centerLon; }
    public void setCenterLon(Double centerLon) { this.centerLon = centerLon; }

    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
