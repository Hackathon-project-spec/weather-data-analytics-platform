package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class TelemetryEvent {
    private String stationId;
    private String stationCode;
    private String stationName;
    private String state;
    private String district;
    private double latitude;
    private double longitude;
    private double temperature;      // °C
    private double humidity;         // %
    private double pressure;         // hPa
    private double precipitationMm;  // mm/hr
    private double windSpeedKmh;     // km/h
    private double windDirection;    // degrees
    private double solarRadiation;   // W/m2
    private int aqi;                 // Air Quality Index
    private boolean isSimulated;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public TelemetryEvent() {
        this.timestamp = Instant.now();
    }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }

    public double getPrecipitationMm() { return precipitationMm; }
    public void setPrecipitationMm(double precipitationMm) { this.precipitationMm = precipitationMm; }

    public double getWindSpeedKmh() { return windSpeedKmh; }
    public void setWindSpeedKmh(double windSpeedKmh) { this.windSpeedKmh = windSpeedKmh; }

    public double getWindDirection() { return windDirection; }
    public void setWindDirection(double windDirection) { this.windDirection = windDirection; }

    public double getSolarRadiation() { return solarRadiation; }
    public void setSolarRadiation(double solarRadiation) { this.solarRadiation = solarRadiation; }

    public int getAqi() { return aqi; }
    public void setAqi(int aqi) { this.aqi = aqi; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
