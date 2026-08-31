package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScoreBreakdown {
    private double sensorMatchPoints;      // Max 40
    private double spatialProximityPoints;  // Max 25
    private double temporalAlignmentPoints; // Max 15
    private double socialCorroborationPoints; // Max 10
    private double consensusPoints;          // Max 10
    private double totalScore;               // 0 to 100
    private String reasoning;
    private String methodologyNote = "PROTOTYPE scoring methodology for demonstration, not an official MoES/IMD verification standard.";

    public ScoreBreakdown() {}

    public ScoreBreakdown(double sensorMatchPoints, double spatialProximityPoints, double temporalAlignmentPoints,
                          double socialCorroborationPoints, double consensusPoints, double totalScore, String reasoning) {
        this.sensorMatchPoints = sensorMatchPoints;
        this.spatialProximityPoints = spatialProximityPoints;
        this.temporalAlignmentPoints = temporalAlignmentPoints;
        this.socialCorroborationPoints = socialCorroborationPoints;
        this.consensusPoints = consensusPoints;
        this.totalScore = totalScore;
        this.reasoning = reasoning;
    }

    public double getSensorMatchPoints() { return sensorMatchPoints; }
    public void setSensorMatchPoints(double sensorMatchPoints) { this.sensorMatchPoints = sensorMatchPoints; }

    public double getSpatialProximityPoints() { return spatialProximityPoints; }
    public void setSpatialProximityPoints(double spatialProximityPoints) { this.spatialProximityPoints = spatialProximityPoints; }

    public double getTemporalAlignmentPoints() { return temporalAlignmentPoints; }
    public void setTemporalAlignmentPoints(double temporalAlignmentPoints) { this.temporalAlignmentPoints = temporalAlignmentPoints; }

    public double getSocialCorroborationPoints() { return socialCorroborationPoints; }
    public void setSocialCorroborationPoints(double socialCorroborationPoints) { this.socialCorroborationPoints = socialCorroborationPoints; }

    public double getConsensusPoints() { return consensusPoints; }
    public void setConsensusPoints(double consensusPoints) { this.consensusPoints = consensusPoints; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getMethodologyNote() { return methodologyNote; }
    public void setMethodologyNote(String methodologyNote) { this.methodologyNote = methodologyNote; }
}
