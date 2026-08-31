package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import java.time.Instant;

public class VerifiedReportEvent {
    private String reportId;
    private CitizenReportEvent originalReport;
    private double confidenceScore;
    private VerificationStatus status;
    private ScoreBreakdown scoreBreakdown;
    private String reasoning;
    private String matchedStationId;
    private String matchedStationName;
    private double stationDistanceKm;
    private long latencyMs;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant verifiedAt;

    public VerifiedReportEvent() {
        this.verifiedAt = Instant.now();
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public CitizenReportEvent getOriginalReport() { return originalReport; }
    public void setOriginalReport(CitizenReportEvent originalReport) { this.originalReport = originalReport; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }

    public ScoreBreakdown getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(ScoreBreakdown scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getMatchedStationId() { return matchedStationId; }
    public void setMatchedStationId(String matchedStationId) { this.matchedStationId = matchedStationId; }

    public String getMatchedStationName() { return matchedStationName; }
    public void setMatchedStationName(String matchedStationName) { this.matchedStationName = matchedStationName; }

    public double getStationDistanceKm() { return stationDistanceKm; }
    public void setStationDistanceKm(double stationDistanceKm) { this.stationDistanceKm = stationDistanceKm; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
}
