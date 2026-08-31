package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import java.time.Instant;

public class WeatherAlertEvent {
    private String alertId;
    private String identifier;
    private String sender = "MoES-IMD-Analytics-DSS";
    private AlertSeverity severity;
    private DisasterCategory category;
    private String headline;
    private String description;
    private String instruction;
    private String affectedState;
    private String affectedDistrict;
    private double centerLat;
    private double centerLon;
    private double radiusKm;
    private String polygonGeoJson;
    private boolean isActive = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant effectiveFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiresAt;

    public WeatherAlertEvent() {
        this.sentAt = Instant.now();
        this.effectiveFrom = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(86400);
        this.isActive = true;
    }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public DisasterCategory getCategory() { return category; }
    public void setCategory(DisasterCategory category) { this.category = category; }

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

    public double getCenterLat() { return centerLat; }
    public void setCenterLat(double centerLat) { this.centerLat = centerLat; }

    public double getCenterLon() { return centerLon; }
    public void setCenterLon(double centerLon) { this.centerLon = centerLon; }

    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }

    public String getPolygonGeoJson() { return polygonGeoJson; }
    public void setPolygonGeoJson(String polygonGeoJson) { this.polygonGeoJson = polygonGeoJson; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
