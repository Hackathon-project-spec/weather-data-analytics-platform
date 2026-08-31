package in.gov.moes.sih26069.citizen.entity;

import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "citizen_reports")
public class CitizenReportEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "reporter_name", length = 128, nullable = false)
    private String reporterName = "Anonymous Citizen";

    @Column(name = "reporter_contact_hash", length = 128)
    private String reporterContactHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 64, nullable = false)
    private DisasterCategory category;

    @Column(name = "severity_level", nullable = false)
    private int severityLevel;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "state", length = 64, nullable = false)
    private String state;

    @Column(name = "district", length = 64, nullable = false)
    private String district;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 32, nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "confidence_score")
    private double confidenceScore = 0.0;

    @Column(name = "score_breakdown", columnDefinition = "TEXT")
    private String scoreBreakdown;

    @Column(name = "verification_reasoning", columnDefinition = "TEXT")
    private String verificationReasoning;

    @Column(name = "matched_station_id", length = 64)
    private String matchedStationId;

    @Column(name = "station_distance_km")
    private Double stationDistanceKm;

    @Column(name = "verification_latency_ms")
    private long verificationLatencyMs = 0;

    @Column(name = "upvotes")
    private int upvotes = 0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "verified_at")
    private Instant verifiedAt;

    public CitizenReportEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterContactHash() { return reporterContactHash; }
    public void setReporterContactHash(String reporterContactHash) { this.reporterContactHash = reporterContactHash; }

    public DisasterCategory getCategory() { return category; }
    public void setCategory(DisasterCategory category) { this.category = category; }

    public int getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(int severityLevel) { this.severityLevel = severityLevel; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(String scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public String getVerificationReasoning() { return verificationReasoning; }
    public void setVerificationReasoning(String verificationReasoning) { this.verificationReasoning = verificationReasoning; }

    public String getMatchedStationId() { return matchedStationId; }
    public void setMatchedStationId(String matchedStationId) { this.matchedStationId = matchedStationId; }

    public Double getStationDistanceKm() { return stationDistanceKm; }
    public void setStationDistanceKm(Double stationDistanceKm) { this.stationDistanceKm = stationDistanceKm; }

    public long getVerificationLatencyMs() { return verificationLatencyMs; }
    public void setVerificationLatencyMs(long verificationLatencyMs) { this.verificationLatencyMs = verificationLatencyMs; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
}
