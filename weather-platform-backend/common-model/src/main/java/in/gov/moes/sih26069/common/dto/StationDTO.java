package in.gov.moes.sih26069.common.dto;

import in.gov.moes.sih26069.common.enums.StationType;
import java.time.Instant;

public class StationDTO {
    private String id;
    private String code;
    private String name;
    private String state;
    private String district;
    private double latitude;
    private double longitude;
    private double altitudeM;
    private StationType stationType;
    private String status;
    private double currentTemperature;
    private double currentRainfallMm;
    private double currentHumidity;
    private double currentWindSpeedKmh;
    private double currentPressure;
    private Instant lastPingAt;

    public StationDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getAltitudeM() { return altitudeM; }
    public void setAltitudeM(double altitudeM) { this.altitudeM = altitudeM; }

    public StationType getStationType() { return stationType; }
    public void setStationType(StationType stationType) { this.stationType = stationType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentTemperature() { return currentTemperature; }
    public void setCurrentTemperature(double currentTemperature) { this.currentTemperature = currentTemperature; }

    public double getCurrentRainfallMm() { return currentRainfallMm; }
    public void setCurrentRainfallMm(double currentRainfallMm) { this.currentRainfallMm = currentRainfallMm; }

    public double getCurrentHumidity() { return currentHumidity; }
    public void setCurrentHumidity(double currentHumidity) { this.currentHumidity = currentHumidity; }

    public double getCurrentWindSpeedKmh() { return currentWindSpeedKmh; }
    public void setCurrentWindSpeedKmh(double currentWindSpeedKmh) { this.currentWindSpeedKmh = currentWindSpeedKmh; }

    public double getCurrentPressure() { return currentPressure; }
    public void setCurrentPressure(double currentPressure) { this.currentPressure = currentPressure; }

    public Instant getLastPingAt() { return lastPingAt; }
    public void setLastPingAt(Instant lastPingAt) { this.lastPingAt = lastPingAt; }
}
