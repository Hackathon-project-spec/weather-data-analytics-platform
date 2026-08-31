package in.gov.moes.sih26069.common.dto;

import java.time.Instant;

public class SystemStatsDTO {
    private int activeStationsCount;
    private long totalTelemetryCount;
    private long totalCitizenReportsCount;
    private long verifiedReportsCount;
    private long suspiciousReportsCount;
    private long debunkedReportsCount;
    private double verificationAccuracyPercent;
    private double currentIngestionRateEventsSec;
    private long averageVerificationLatencyMs;
    private int activeAlertsCount;
    private Instant timestamp;

    public SystemStatsDTO() {
        this.timestamp = Instant.now();
    }

    public int getActiveStationsCount() { return activeStationsCount; }
    public void setActiveStationsCount(int activeStationsCount) { this.activeStationsCount = activeStationsCount; }

    public long getTotalTelemetryCount() { return totalTelemetryCount; }
    public void setTotalTelemetryCount(long totalTelemetryCount) { this.totalTelemetryCount = totalTelemetryCount; }

    public long getTotalCitizenReportsCount() { return totalCitizenReportsCount; }
    public void setTotalCitizenReportsCount(long totalCitizenReportsCount) { this.totalCitizenReportsCount = totalCitizenReportsCount; }

    public long getVerifiedReportsCount() { return verifiedReportsCount; }
    public void setVerifiedReportsCount(long verifiedReportsCount) { this.verifiedReportsCount = verifiedReportsCount; }

    public long getSuspiciousReportsCount() { return suspiciousReportsCount; }
    public void setSuspiciousReportsCount(long suspiciousReportsCount) { this.suspiciousReportsCount = suspiciousReportsCount; }

    public long getDebunkedReportsCount() { return debunkedReportsCount; }
    public void setDebunkedReportsCount(long debunkedReportsCount) { this.debunkedReportsCount = debunkedReportsCount; }

    public double getVerificationAccuracyPercent() { return verificationAccuracyPercent; }
    public void setVerificationAccuracyPercent(double verificationAccuracyPercent) { this.verificationAccuracyPercent = verificationAccuracyPercent; }

    public double getCurrentIngestionRateEventsSec() { return currentIngestionRateEventsSec; }
    public void setCurrentIngestionRateEventsSec(double currentIngestionRateEventsSec) { this.currentIngestionRateEventsSec = currentIngestionRateEventsSec; }

    public long getAverageVerificationLatencyMs() { return averageVerificationLatencyMs; }
    public void setAverageVerificationLatencyMs(long averageVerificationLatencyMs) { this.averageVerificationLatencyMs = averageVerificationLatencyMs; }

    public int getActiveAlertsCount() { return activeAlertsCount; }
    public void setActiveAlertsCount(int activeAlertsCount) { this.activeAlertsCount = activeAlertsCount; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
