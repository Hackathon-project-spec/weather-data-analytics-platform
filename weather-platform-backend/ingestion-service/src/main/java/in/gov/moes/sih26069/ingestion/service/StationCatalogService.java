package in.gov.moes.sih26069.ingestion.service;

import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.enums.StationType;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StationCatalogService {

    private final Map<String, StationDTO> stationMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCatalog() {
        addStation("stn-mum-01", "BOM-COL", "Mumbai Colaba AWS", "Maharashtra", "Mumbai City", 18.8997, 72.8153, 11.0, StationType.AWS, 29.5, 78.0, 1008.5, 4.2, 14.0);
        addStation("stn-mum-02", "BOM-SCZ", "Mumbai Santacruz AWS", "Maharashtra", "Mumbai Suburban", 19.0896, 72.8656, 14.0, StationType.AWS, 30.1, 75.0, 1007.8, 6.5, 12.0);
        addStation("stn-mum-03", "BOM-THN", "Thane City AWS", "Maharashtra", "Thane", 19.2183, 72.9781, 15.0, StationType.AWS, 30.5, 74.0, 1007.2, 5.0, 10.0);
        
        addStation("stn-del-01", "DEL-SFD", "Delhi Safdarjung AWS", "Delhi", "New Delhi", 28.5843, 77.2065, 216.0, StationType.AWS, 34.2, 45.0, 1004.0, 0.0, 8.0);
        addStation("stn-del-02", "DEL-PAL", "Delhi Palam AWS", "Delhi", "South West Delhi", 28.5630, 77.1200, 237.0, StationType.AWS, 35.0, 42.0, 1003.5, 0.0, 9.0);
        addStation("stn-del-03", "DEL-NOI", "Noida Sector 62 AWS", "Uttar Pradesh", "Gautam Buddha Nagar", 28.6270, 77.3725, 200.0, StationType.AWS, 34.8, 44.0, 1003.8, 0.0, 7.5);
        addStation("stn-del-04", "DEL-GUR", "Gurugram Cyber City AWS", "Haryana", "Gurugram", 28.4595, 77.0266, 219.0, StationType.AWS, 35.4, 41.0, 1003.2, 0.0, 8.5);

        addStation("stn-blr-01", "BLR-CTY", "Bengaluru City AWS", "Karnataka", "Bengaluru Urban", 12.9716, 77.5946, 920.0, StationType.AWS, 24.5, 68.0, 915.0, 2.0, 15.0);
        addStation("stn-blr-02", "BLR-KIAL", "Bengaluru Airport AWS", "Karnataka", "Bengaluru Rural", 13.1986, 77.7066, 915.0, StationType.AWS, 23.8, 70.0, 914.0, 1.5, 18.0);

        addStation("stn-chn-01", "MAA-MNG", "Chennai Meenambakkam AWS", "Tamil Nadu", "Chennai", 12.9830, 80.1700, 16.0, StationType.AWS, 33.0, 65.0, 1010.0, 0.0, 11.0);
        addStation("stn-chn-02", "MAA-NUM", "Chennai Nungambakkam AWS", "Tamil Nadu", "Chennai", 13.0600, 80.2400, 10.0, StationType.AWS, 33.4, 63.0, 1010.5, 0.0, 10.0);

        addStation("stn-kol-01", "CCU-ALR", "Kolkata Alipore AWS", "West Bengal", "Kolkata", 22.5333, 88.3333, 6.0, StationType.AWS, 31.0, 82.0, 1006.0, 8.0, 12.0);
        addStation("stn-kol-02", "CCU-DUM", "Kolkata Dum Dum AWS", "West Bengal", "North 24 Parganas", 22.6500, 88.4500, 5.0, StationType.AWS, 31.2, 80.0, 1005.5, 6.5, 14.0);

        addStation("stn-hyd-01", "HYD-BEG", "Hyderabad Begumpet AWS", "Telangana", "Hyderabad", 17.4500, 78.4700, 531.0, StationType.AWS, 28.0, 60.0, 955.0, 0.5, 9.0);
        addStation("stn-hyd-02", "HYD-RGI", "Hyderabad Shamshabad AWS", "Telangana", "Rangareddy", 17.2403, 78.4294, 617.0, StationType.AWS, 27.5, 62.0, 953.0, 0.0, 11.0);

        addStation("stn-odi-01", "BBI-CTY", "Bhubaneswar AWS", "Odisha", "Khurda", 20.2961, 85.8245, 45.0, StationType.AWS, 29.0, 85.0, 1004.0, 12.0, 22.0);
        addStation("stn-odi-02", "BBI-PUR", "Puri Coastal AWS", "Odisha", "Puri", 19.8135, 85.8312, 10.0, StationType.AWS, 28.5, 88.0, 1001.0, 18.0, 35.0);
        addStation("stn-odi-03", "BBI-PRD", "Paradip Port AWS", "Odisha", "Jagatsinghpur", 20.3167, 86.6167, 4.0, StationType.AWS, 28.0, 90.0, 999.5, 24.0, 42.0);
        addStation("stn-odi-04", "BBI-BAL", "Balasore Radar Station", "Odisha", "Balasore", 21.4934, 86.9135, 19.0, StationType.RADAR, 28.2, 87.0, 1002.0, 15.0, 30.0);

        addStation("stn-ahm-01", "AMD-CTY", "Ahmedabad City AWS", "Gujarat", "Ahmedabad", 23.0225, 72.5714, 53.0, StationType.AWS, 35.0, 50.0, 1006.0, 0.0, 7.0);
        addStation("stn-pun-01", "PNQ-SHV", "Pune Shivajinagar AWS", "Maharashtra", "Pune", 18.5314, 73.8446, 560.0, StationType.AWS, 26.5, 72.0, 950.0, 3.5, 10.0);
        addStation("stn-jai-01", "JAI-SNG", "Jaipur Sanganer AWS", "Rajasthan", "Jaipur", 26.8200, 75.8000, 390.0, StationType.AWS, 36.2, 38.0, 965.0, 0.0, 8.0);
        addStation("stn-lko-01", "LKO-AMA", "Lucknow Amausi AWS", "Uttar Pradesh", "Lucknow", 26.7606, 80.8893, 123.0, StationType.AWS, 32.5, 58.0, 995.0, 1.2, 6.0);
        addStation("stn-pat-01", "PAT-CTY", "Patna Airport AWS", "Bihar", "Patna", 25.5941, 85.1376, 53.0, StationType.AWS, 32.0, 64.0, 1002.0, 2.0, 7.0);
        addStation("stn-gau-01", "GAU-BOR", "Guwahati Borjhar AWS", "Assam", "Kamrup Metropolitan", 26.1061, 91.5859, 54.0, StationType.AWS, 27.0, 89.0, 1003.0, 14.0, 8.0);
        addStation("stn-koc-01", "COK-NED", "Kochi Nedumbassery AWS", "Kerala", "Ernakulam", 10.1518, 76.3930, 8.0, StationType.AWS, 28.8, 84.0, 1010.0, 11.0, 12.0);
        addStation("stn-viz-01", "VTZ-CTY", "Visakhapatnam AWS", "Andhra Pradesh", "Visakhapatnam", 17.6868, 83.2185, 4.0, StationType.AWS, 30.5, 76.0, 1008.0, 3.0, 16.0);
        addStation("stn-bho-01", "BHO-BAIR", "Bhopal Bairagarh AWS", "Madhya Pradesh", "Bhopal", 23.2800, 77.3500, 523.0, StationType.AWS, 31.0, 55.0, 958.0, 0.0, 6.5);
        addStation("stn-shi-01", "SLV-CTY", "Shimla Ridge AWS", "Himachal Pradesh", "Shimla", 31.1048, 77.1734, 2205.0, StationType.AWS, 16.0, 70.0, 785.0, 4.0, 9.0);
        addStation("stn-sri-01", "SXR-CTY", "Srinagar Aerodrome AWS", "Jammu and Kashmir", "Srinagar", 34.0837, 74.7973, 1585.0, StationType.AWS, 19.5, 60.0, 840.0, 1.0, 7.0);
    }

    private void addStation(String id, String code, String name, String state, String district,
                            double lat, double lon, double alt, StationType type,
                            double temp, double humidity, double pressure, double rainMm, double windKmh) {
        StationDTO s = new StationDTO();
        s.setId(id);
        s.setCode(code);
        s.setName(name);
        s.setState(state);
        s.setDistrict(district);
        s.setLatitude(lat);
        s.setLongitude(lon);
        s.setAltitudeM(alt);
        s.setStationType(type);
        s.setStatus("ACTIVE");
        s.setCurrentTemperature(temp);
        s.setCurrentHumidity(humidity);
        s.setCurrentPressure(pressure);
        s.setCurrentRainfallMm(rainMm);
        s.setCurrentWindSpeedKmh(windKmh);
        s.setLastPingAt(Instant.now());
        stationMap.put(id, s);
    }

    public List<StationDTO> getAllStations() {
        return new ArrayList<>(stationMap.values());
    }

    public Optional<StationDTO> getStation(String id) {
        return Optional.ofNullable(stationMap.get(id));
    }

    public void updateStationMetrics(TelemetryEvent event) {
        StationDTO s = stationMap.get(event.getStationId());
        if (s != null) {
            s.setCurrentTemperature(event.getTemperature());
            s.setCurrentHumidity(event.getHumidity());
            s.setCurrentPressure(event.getPressure());
            s.setCurrentRainfallMm(event.getPrecipitationMm());
            s.setCurrentWindSpeedKmh(event.getWindSpeedKmh());
            s.setLastPingAt(event.getTimestamp());
        }
    }
}
