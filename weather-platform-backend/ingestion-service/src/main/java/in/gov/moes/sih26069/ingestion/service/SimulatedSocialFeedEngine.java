package in.gov.moes.sih26069.ingestion.service;

import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.event.SocialFeedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class SimulatedSocialFeedEngine {

    private static final Logger log = LoggerFactory.getLogger(SimulatedSocialFeedEngine.class);

    @Autowired(required = false)
    private KafkaTemplate<String, SocialFeedEvent> kafkaTemplate;

    private final Random random = new Random();

    private final List<SocialFeedTemplate> templates = List.of(
        new SocialFeedTemplate("Maharashtra", "Mumbai City", 18.99, 72.82, DisasterCategory.HEAVY_RAIN,
            "Waterlogging reported near Dadar TT circle after continuous downpour. Stay cautious! [SIMULATED]", List.of("#IMD", "#MumbaiRains", "#TrafficAlert"), -0.6),
        new SocialFeedTemplate("Maharashtra", "Mumbai Suburban", 19.09, 72.86, DisasterCategory.FLOOD,
            "High tide combined with intense rainfall causing water buildup along Western Express Highway. [SIMULATED]", List.of("#IMD", "#MumbaiWeather", "#Flood"), -0.8),
        new SocialFeedTemplate("Odisha", "Puri", 19.81, 85.83, DisasterCategory.CYCLONE_WIND,
            "Strong coastal squall hitting Puri beach with gale force winds. Fishermen advised not to venture out. [SIMULATED]", List.of("#IMDAlert", "#OdishaWeather", "#CycloneWatch"), -0.7),
        new SocialFeedTemplate("Delhi", "New Delhi", 28.58, 77.20, DisasterCategory.HEATWAVE,
            "Blistering afternoon heat in Delhi NCR, surface temps touching 46°C. Stay hydrated! [SIMULATED]", List.of("#IMD", "#DelhiHeatwave", "#WeatherUpdate"), -0.5),
        new SocialFeedTemplate("Karnataka", "Bengaluru Urban", 12.97, 77.59, DisasterCategory.HEAVY_RAIN,
            "Sudden evening thundershowers cooling down Bengaluru after a warm day. [SIMULATED]", List.of("#BengaluruRains", "#IMD", "#NammaBengaluru"), 0.2),
        new SocialFeedTemplate("West Bengal", "Kolkata", 22.53, 88.33, DisasterCategory.HEAVY_RAIN,
            "Heavy cloud cover and moderate rain observed over central Kolkata. [SIMULATED]", List.of("#KolkataWeather", "#IMD", "#Monsoon"), 0.0),
        new SocialFeedTemplate("Assam", "Kamrup Metropolitan", 26.10, 91.58, DisasterCategory.FLOOD,
            "Brahmaputra water level rising steadily following catchment area rainfall. [SIMULATED]", List.of("#AssamFloods", "#IMDAlert", "#DisasterManagement"), -0.7)
    );

    @Scheduled(fixedRate = 6000)
    public void generateSimulatedPost() {
        SocialFeedTemplate t = templates.get(random.nextInt(templates.size()));

        SocialFeedEvent event = new SocialFeedEvent();
        event.setPostId("sim-post-" + UUID.randomUUID().toString().substring(0, 8));
        event.setPlatform("Twitter/X (Simulated)");
        event.setAuthorHandle("@citizen_observer_" + (100 + random.nextInt(900)));
        event.setState(t.state);
        event.setDistrict(t.district);
        event.setLatitude(t.lat + (random.nextDouble() - 0.5) * 0.02);
        event.setLongitude(t.lon + (random.nextDouble() - 0.5) * 0.02);
        event.setDisasterCategory(t.category);
        event.setText(t.text);
        event.setHashtags(t.hashtags);
        event.setSentimentScore(t.sentiment);
        event.setSimulated(true);
        event.setTimestamp(Instant.now());

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("weather.social.feed", event.getPostId(), event);
            } catch (Exception e) {
                log.trace("Kafka social feed send error: {}", e.getMessage());
            }
        }
    }

    public void injectScenarioSocialFeed(String state, String district, double lat, double lon,
                                         DisasterCategory category, String text, List<String> hashtags, double sentiment) {
        SocialFeedEvent event = new SocialFeedEvent();
        event.setPostId("sim-scenario-" + UUID.randomUUID().toString().substring(0, 8));
        event.setPlatform("Twitter/X (Simulated Scenario)");
        event.setAuthorHandle("@emergency_citizen_" + (100 + random.nextInt(900)));
        event.setState(state);
        event.setDistrict(district);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setDisasterCategory(category);
        event.setText(text);
        event.setHashtags(hashtags);
        event.setSentimentScore(sentiment);
        event.setSimulated(true);
        event.setTimestamp(Instant.now());

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("weather.social.feed", event.getPostId(), event);
            } catch (Exception e) {
                log.trace("Kafka social feed send error: {}", e.getMessage());
            }
        }
    }

    private record SocialFeedTemplate(String state, String district, double lat, double lon,
                                      DisasterCategory category, String text, List<String> hashtags, double sentiment) {}
}
