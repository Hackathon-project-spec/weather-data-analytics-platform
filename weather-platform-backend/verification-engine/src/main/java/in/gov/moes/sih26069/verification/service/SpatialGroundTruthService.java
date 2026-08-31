package in.gov.moes.sih26069.verification.service;

import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.enums.StationType;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpatialGroundTruthService {

    private static final Logger log = LoggerFactory.getLogger(SpatialGroundTruthService.class);

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${app.ingestion.url:http://localhost:8081}")
    private String ingestionServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, StationDTO> localStationCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initStations() {
        addFallbackStation("stn-mum-01", "BOM-COL", "Mumbai Colaba AWS", "Maharashtra", "Mumbai City", 18.8997, 72.8153, 29.5, 78.0, 1008.5, 4.2, 14.0);
        addFallbackStation("stn-mum-02", "BOM-SCZ", "Mumbai Santacruz AWS", "Maharashtra", "Mumbai Suburban", 19.0896, 72.8656, 30.1, 75.0, 1007.8, 6.5, 12.0);
        addFallbackStation("stn-mum-03", "BOM-THN", "Thane City AWS", "Maharashtra", "Thane", 19.2183, 72.9781, 30.5, 74.0, 1007.2, 5.0, 10.0);
        addFallbackStation("stn-del-01", "DEL-SFD", "Delhi Safdarjung AWS", "Delhi", "New Delhi", 28.5843, 77.2065, 34.2, 45.0, 1004.0, 0.0, 8.0);
        addFallbackStation("stn-del-02", "DEL-PAL", "Delhi Palam AWS", "Delhi", "South West Delhi", 28.5630, 77.1200, 35.0, 42.0, 1003.5, 0.0, 9.0);
        addFallbackStation("stn-del-03", "DEL-NOI", "Noida Sector 62 AWS", "Uttar Pradesh", "Gautam Buddha Nagar", 28.6270, 77.3725, 34.8, 44.0, 1003.8, 0.0, 7.5);
        addFallbackStation("stn-blr-01", "BLR-CTY", "Bengaluru City AWS", "Karnataka", "Bengaluru Urban", 12.9716, 77.5946, 24.5, 68.0, 915.0, 2.0, 15.0);
        addFallbackStation("stn-chn-01", "MAA-MNG", "Chennai Meenambakkam AWS", "Tamil Nadu", "Chennai", 12.9830, 80.1700, 33.0, 65.0, 1010.0, 0.0, 11.0);
        addFallbackStation("stn-chn-02", "MAA-NUM", "Chennai Nungambakkam AWS", "Tamil Nadu", "Chennai", 13.0600, 80.2400, 33.4, 63.0, 1010.5, 0.0, 10.0);
        addFallbackStation("stn-kol-01", "CCU-ALR", "Kolkata Alipore AWS", "West Bengal", "Kolkata", 22.5333, 88.3333, 31.0, 82.0, 1006.0, 8.0, 12.0);
        addFallbackStation("stn-odi-01", "BBI-CTY", "Bhubaneswar AWS", "Odisha", "Khurda", 20.2961, 85.8245, 29.0, 85.0, 1004.0, 12.0, 22.0);
        addFallbackStation("stn-odi-02", "BBI-PUR", "Puri Coastal AWS", "Odisha", "Puri", 19.8135, 85.8312, 28.5, 88.0, 1001.0, 18.0, 35.0);
        addFallbackStation("stn-odi-03", "BBI-PRD", "Paradip Port AWS", "Odisha", "Jagatsinghpur", 20.3167, 86.6167, 28.0, 90.0, 999.5, 24.0, 42.0);
        addFallbackStation("stn-ahm-01", "AMD-CTY", "Ahmedabad City AWS", "Gujarat", "Ahmedabad", 23.0225, 72.5714, 35.0, 50.0, 1006.0, 0.0, 7.0);
        addFallbackStation("stn-pun-01", "PNQ-SHV", "Pune Shivajinagar AWS", "Maharashtra", "Pune", 18.5314, 73.8446, 26.5, 72.0, 950.0, 3.5, 10.0);
        addFallbackStation("stn-jai-01", "JAI-SNG", "Jaipur Sanganer AWS", "Rajasthan", "Jaipur", 26.8200, 75.8000, 36.2, 38.0, 965.0, 0.0, 8.0);
        addFallbackStation("stn-lko-01", "LKO-AMA", "Lucknow Amausi AWS", "Uttar Pradesh", "Lucknow", 26.7606, 80.8893, 32.5, 58.0, 995.0, 1.2, 6.0);
        addFallbackStation("stn-pat-01", "PAT-CTY", "Patna Airport AWS", "Bihar", "Patna", 25.5941, 85.1376, 32.0, 64.0, 1002.0, 2.0, 7.0);
        addFallbackStation("stn-gau-01", "GAU-BOR", "Guwahati Borjhar AWS", "Assam", "Kamrup Metropolitan", 26.1061, 91.5859, 27.0, 89.0, 1003.0, 14.0, 8.0);
        addFallbackStation("stn-koc-01", "COK-NED", "Kochi Nedumbassery AWS", "Kerala", "Ernakulam", 10.1518, 76.3930, 28.8, 84.0, 1010.0, 11.0, 12.0);
    }

    private void addFallbackStation(String id, String code, String name, String state, String district,
                                    double lat, double lon, double temp, double humidity, double pressure, double rain, double wind) {
        StationDTO s = new StationDTO();
        s.setId(id);
        s.setCode(code);
        s.setName(name);
        s.setState(state);
        s.setDistrict(district);
        s.setLatitude(lat);
        s.setLongitude(lon);
        s.setStationType(StationType.AWS);
        s.setStatus("ACTIVE");
        s.setCurrentTemperature(temp);
        s.setCurrentHumidity(humidity);
        s.setCurrentPressure(pressure);
        s.setCurrentRainfallMm(rain);
        s.setCurrentWindSpeedKmh(wind);
        s.setLastPingAt(Instant.now());
        localStationCache.put(id, s);
    }

    public record StationMatch(StationDTO station, double distanceKm) {}

    public Optional<StationMatch> findNearestStation(double lat, double lon, double maxRadiusKm) {
        // First try to refresh from ingestion service if reachable
        trySyncStationsFromIngestion();

        StationDTO nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (StationDTO station : localStationCache.values()) {
            double dist = calculateHaversineDistanceKm(lat, lon, station.getLatitude(), station.getLongitude());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = station;
            }
        }

        if (nearest != null && minDistance <= maxRadiusKm) {
            return Optional.of(new StationMatch(nearest, Math.round(minDistance * 10.0) / 10.0));
        }

        return Optional.empty();
    }

    public void updateStationFromTelemetry(TelemetryEvent event) {
        StationDTO s = localStationCache.get(event.getStationId());
        if (s != null) {
            s.setCurrentTemperature(event.getTemperature());
            s.setCurrentHumidity(event.getHumidity());
            s.setCurrentPressure(event.getPressure());
            s.setCurrentRainfallMm(event.getPrecipitationMm());
            s.setCurrentWindSpeedKmh(event.getWindSpeedKmh());
            s.setLastPingAt(event.getTimestamp());
        }
    }

    private void trySyncStationsFromIngestion() {
        try {
            StationDTO[] stations = restTemplate.getForObject(ingestionServiceUrl + "/api/v1/stations", StationDTO[].class);
            if (stations != null) {
                for (StationDTO stn : stations) {
                    localStationCache.put(stn.getId(), stn);
                }
            }
        } catch (Exception ignored) {}
    }

    public double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
