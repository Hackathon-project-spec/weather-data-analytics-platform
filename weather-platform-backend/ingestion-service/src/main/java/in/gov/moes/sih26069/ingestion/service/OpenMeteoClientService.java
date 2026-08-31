package in.gov.moes.sih26069.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class OpenMeteoClientService {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClientService.class);

    @Autowired
    private StationCatalogService catalogService;

    @Autowired(required = false)
    private KafkaTemplate<String, TelemetryEvent> kafkaTemplate;

    @Value("${app.openmeteo.enabled:true}")
    private boolean openMeteoEnabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Sync live data from Open-Meteo for top stations every 5 minutes
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void syncLiveWeatherFromOpenMeteo() {
        if (!openMeteoEnabled) return;
        List<StationDTO> stations = catalogService.getAllStations();
        // Sample 5 stations per cycle to stay within Open-Meteo rate limits
        for (int i = 0; i < Math.min(stations.size(), 6); i++) {
            StationDTO stn = stations.get(i);
            try {
                String url = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,surface_pressure,precipitation,wind_speed_10m,wind_direction_10m",
                    stn.getLatitude(), stn.getLongitude()
                );
                String response = restTemplate.getForObject(url, String.class);
                if (response != null) {
                    JsonNode root = objectMapper.readTree(response);
                    JsonNode current = root.path("current");
                    if (!current.isMissingNode()) {
                        TelemetryEvent event = new TelemetryEvent();
                        event.setStationId(stn.getId());
                        event.setStationCode(stn.getCode());
                        event.setStationName(stn.getName());
                        event.setState(stn.getState());
                        event.setDistrict(stn.getDistrict());
                        event.setLatitude(stn.getLatitude());
                        event.setLongitude(stn.getLongitude());
                        event.setTemperature(current.path("temperature_2m").asDouble(stn.getCurrentTemperature()));
                        event.setHumidity(current.path("relative_humidity_2m").asDouble(stn.getCurrentHumidity()));
                        event.setPressure(current.path("surface_pressure").asDouble(stn.getCurrentPressure()));
                        event.setPrecipitationMm(current.path("precipitation").asDouble(stn.getCurrentRainfallMm()));
                        event.setWindSpeedKmh(current.path("wind_speed_10m").asDouble(stn.getCurrentWindSpeedKmh()));
                        event.setWindDirection(current.path("wind_direction_10m").asDouble(0.0));
                        event.setAqi(45);
                        event.setSimulated(false);
                        event.setTimestamp(Instant.now());

                        catalogService.updateStationMetrics(event);

                        if (kafkaTemplate != null) {
                            try {
                                kafkaTemplate.send("weather.raw.telemetry", event.getStationId(), event);
                            } catch (Exception e) {
                                log.debug("Kafka send raw telemetry (OpenMeteo): {}", e.getMessage());
                            }
                        }
                        log.info("Synced live Open-Meteo telemetry for {}: {}°C, {} mm/hr rain", stn.getName(), event.getTemperature(), event.getPrecipitationMm());
                    }
                }
            } catch (Exception e) {
                log.warn("Open-Meteo sync skipped for {}: {}", stn.getName(), e.getMessage());
            }
        }
    }
}
