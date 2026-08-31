package in.gov.moes.sih26069.common.dto;

import java.time.Instant;

public class TimeSeriesPoint {
    private Instant timestamp;
    private double temperature;
    private double precipitationMm;
    private double windSpeedKmh;
    private double pressure;
    private double historicalPrecipitationAvgMm;

    public TimeSeriesPoint() {}

    public TimeSeriesPoint(Instant timestamp, double temperature, double precipitationMm, double windSpeedKmh, double pressure, double historicalPrecipitationAvgMm) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.precipitationMm = precipitationMm;
        this.windSpeedKmh = windSpeedKmh;
        this.pressure = pressure;
        this.historicalPrecipitationAvgMm = historicalPrecipitationAvgMm;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getPrecipitationMm() { return precipitationMm; }
    public void setPrecipitationMm(double precipitationMm) { this.precipitationMm = precipitationMm; }

    public double getWindSpeedKmh() { return windSpeedKmh; }
    public void setWindSpeedKmh(double windSpeedKmh) { this.windSpeedKmh = windSpeedKmh; }

    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }

    public double getHistoricalPrecipitationAvgMm() { return historicalPrecipitationAvgMm; }
    public void setHistoricalPrecipitationAvgMm(double historicalPrecipitationAvgMm) { this.historicalPrecipitationAvgMm = historicalPrecipitationAvgMm; }
}
