package in.gov.moes.sih26069.ingestion.service;

import in.gov.moes.sih26069.common.dto.ScenarioTriggerRequest;
import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.ScenarioType;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScenarioLabService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioLabService.class);

    @Autowired
    private StationCatalogService catalogService;

    @Autowired
    private SimulatedSocialFeedEngine socialFeedEngine;

    @Autowired(required = false)
    private KafkaTemplate<String, TelemetryEvent> kafkaTemplate;

    private final Map<String, Object> activeScenarioDetails = new ConcurrentHashMap<>();

    public Map<String, Object> triggerScenario(ScenarioTriggerRequest request) {
        ScenarioType type = request.getScenarioType();
        log.info("Triggering Scenario: {}", type);
        activeScenarioDetails.put("activeScenario", type.name());
        activeScenarioDetails.put("triggeredAt", Instant.now().toString());

        switch (type) {
            case MUMBAI_CLOUDBURST -> triggerMumbaiCloudburst();
            case ODISHA_CYCLONE -> triggerOdishaCyclone();
            case DELHI_HEATWAVE -> triggerDelhiHeatwave();
            case FAKE_DISASTER_ATTEMPT -> triggerFakeDisasterAttempt();
            case NORMAL_MONSOON -> triggerNormalMonsoonReset();
        }

        return getActiveScenarioStatus();
    }

    public Map<String, Object> getActiveScenarioStatus() {
        return activeScenarioDetails;
    }

    private void triggerMumbaiCloudburst() {
        log.info("Executing Scenario: Mumbai Cloudburst (85-115 mm/hr precipitation)...");
        injectStationExtremeTelemetry("stn-mum-01", 26.2, 98.0, 996.0, 95.0, 48.0);
        injectStationExtremeTelemetry("stn-mum-02", 25.8, 99.0, 995.5, 110.5, 52.0);
        injectStationExtremeTelemetry("stn-mum-03", 26.0, 97.0, 997.0, 88.0, 42.0);

        socialFeedEngine.injectScenarioSocialFeed(
            "Maharashtra", "Mumbai City", 18.99, 72.82, DisasterCategory.FLOOD,
            "[SIMULATED SCENARIO] Massive cloudburst inundates Dadar, Hindmata, and Parel. Water above 3 feet! #MumbaiRains #Cloudburst #IMDAlert",
            List.of("#MumbaiCloudburst", "#IMDAlert", "#NDRF"), -0.95
        );
        socialFeedEngine.injectScenarioSocialFeed(
            "Maharashtra", "Mumbai Suburban", 19.09, 72.86, DisasterCategory.FLOOD,
            "[SIMULATED SCENARIO] Kurla railway tracks submerged, local trains halted. BMC emergency teams mobilized. #MumbaiFloods",
            List.of("#MumbaiFloods", "#Emergency", "#IMD"), -0.90
        );

        activeScenarioDetails.put("description", "Extreme precipitation (85-110 mm/hr) injected across Mumbai stations with flood social signal corroboration.");
    }

    private void triggerOdishaCyclone() {
        log.info("Executing Scenario: Odisha Super Cyclone (125 km/h winds, 980 hPa)...");
        injectStationExtremeTelemetry("stn-odi-01", 25.0, 95.0, 985.0, 65.0, 85.0);
        injectStationExtremeTelemetry("stn-odi-02", 24.5, 99.0, 980.0, 95.0, 125.0);
        injectStationExtremeTelemetry("stn-odi-03", 24.0, 100.0, 978.0, 115.0, 138.0);
        injectStationExtremeTelemetry("stn-odi-04", 24.8, 98.0, 982.0, 80.0, 110.0);

        socialFeedEngine.injectScenarioSocialFeed(
            "Odisha", "Puri", 19.81, 85.83, DisasterCategory.CYCLONE_WIND,
            "[SIMULATED SCENARIO] Cyclone landfall underway near Puri! Destructive winds tearing roofs, storm surge at beach road. #CycloneAlert #Odisha",
            List.of("#CycloneAlert", "#IMDWarning", "#OdishaCyclone"), -0.98
        );

        activeScenarioDetails.put("description", "Severe tropical cyclone telemetry (125+ km/h winds, 980 hPa barometric drop) injected in Coastal Odisha.");
    }

    private void triggerDelhiHeatwave() {
        log.info("Executing Scenario: Delhi Extreme Heatwave (47.5°C)...");
        injectStationExtremeTelemetry("stn-del-01", 47.2, 22.0, 998.0, 0.0, 15.0);
        injectStationExtremeTelemetry("stn-del-02", 47.8, 19.0, 997.5, 0.0, 18.0);
        injectStationExtremeTelemetry("stn-del-03", 46.9, 21.0, 998.5, 0.0, 12.0);
        injectStationExtremeTelemetry("stn-del-04", 47.5, 20.0, 997.8, 0.0, 14.0);

        socialFeedEngine.injectScenarioSocialFeed(
            "Delhi", "New Delhi", 28.58, 77.20, DisasterCategory.HEATWAVE,
            "[SIMULATED SCENARIO] Delhi records blistering 47.8°C at Palam observatory. Red heatwave alert issued by IMD. #DelhiHeatwave",
            List.of("#DelhiHeatwave", "#RedAlert", "#StaySafe"), -0.65
        );

        activeScenarioDetails.put("description", "Extreme heatwave telemetry (47.5°C+ temperatures) injected in Delhi-NCR stations.");
    }

    private void triggerFakeDisasterAttempt() {
        log.info("Executing Scenario: Coordinated Fake Disaster Reports (Anti-Spam Filter Test)...");
        // Keep Chennai weather completely normal & sunny (34.5°C, 0mm rain)
        injectStationExtremeTelemetry("stn-chn-01", 34.5, 62.0, 1011.0, 0.0, 8.0);
        injectStationExtremeTelemetry("stn-chn-02", 34.8, 60.0, 1011.5, 0.0, 7.5);

        // Inject simulated spam posts claiming fake blizzard
        socialFeedEngine.injectScenarioSocialFeed(
            "Tamil Nadu", "Chennai", 13.06, 80.24, DisasterCategory.BLIZZARD,
            "[SIMULATED BOT SPAM] Unprecedented blizzard and 4 feet of snow reported at Marina Beach Chennai! #ChennaiSnow #FakeNews",
            List.of("#ChennaiSnow", "#BlizzardInMay", "#ViralClaim"), 0.1
        );

        activeScenarioDetails.put("description", "Disinformation attack simulated: Fake blizzard claims injected for Chennai while AWS ground truth shows 34.5°C clear skies. Verification engine should mark reports as DEBUNKED.");
    }

    private void triggerNormalMonsoonReset() {
        log.info("Resetting scenario to Normal Monsoon state...");
        catalogService.initCatalog();
        activeScenarioDetails.put("description", "All stations reset to nominal monsoon conditions.");
    }

    private void injectStationExtremeTelemetry(String stationId, double temp, double humidity,
                                                double pressure, double rainMm, double windKmh) {
        catalogService.getStation(stationId).ifPresent(stn -> {
            TelemetryEvent event = new TelemetryEvent();
            event.setStationId(stn.getId());
            event.setStationCode(stn.getCode());
            event.setStationName(stn.getName());
            event.setState(stn.getState());
            event.setDistrict(stn.getDistrict());
            event.setLatitude(stn.getLatitude());
            event.setLongitude(stn.getLongitude());
            event.setTemperature(temp);
            event.setHumidity(humidity);
            event.setPressure(pressure);
            event.setPrecipitationMm(rainMm);
            event.setWindSpeedKmh(windKmh);
            event.setWindDirection(180.0);
            event.setSolarRadiation(120.0);
            event.setAqi(60);
            event.setSimulated(true);
            event.setTimestamp(Instant.now());

            catalogService.updateStationMetrics(event);

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("weather.raw.telemetry", event.getStationId(), event);
                } catch (Exception e) {
                    log.trace("Kafka send error: {}", e.getMessage());
                }
            }
        });
    }
}
