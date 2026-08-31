package in.gov.moes.sih26069.common.dto;

import in.gov.moes.sih26069.common.enums.ScenarioType;

public class ScenarioTriggerRequest {
    private ScenarioType scenarioType;
    private int durationSeconds = 60;
    private int intensityMultiplier = 1;
    private boolean injectCitizenReports = true;
    private boolean injectSocialFeed = true;

    public ScenarioTriggerRequest() {}

    public ScenarioType getScenarioType() { return scenarioType; }
    public void setScenarioType(ScenarioType scenarioType) { this.scenarioType = scenarioType; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getIntensityMultiplier() { return intensityMultiplier; }
    public void setIntensityMultiplier(int intensityMultiplier) { this.intensityMultiplier = intensityMultiplier; }

    public boolean isInjectCitizenReports() { return injectCitizenReports; }
    public void setInjectCitizenReports(boolean injectCitizenReports) { this.injectCitizenReports = injectCitizenReports; }

    public boolean isInjectSocialFeed() { return injectSocialFeed; }
    public void setInjectSocialFeed(boolean injectSocialFeed) { this.injectSocialFeed = injectSocialFeed; }
}
