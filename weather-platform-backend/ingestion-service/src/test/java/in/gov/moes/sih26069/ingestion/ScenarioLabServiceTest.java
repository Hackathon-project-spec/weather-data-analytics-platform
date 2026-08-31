package in.gov.moes.sih26069.ingestion;

import in.gov.moes.sih26069.common.dto.ScenarioTriggerRequest;
import in.gov.moes.sih26069.common.enums.ScenarioType;
import in.gov.moes.sih26069.ingestion.service.ScenarioLabService;
import in.gov.moes.sih26069.ingestion.service.SimulatedSocialFeedEngine;
import in.gov.moes.sih26069.ingestion.service.StationCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ScenarioLabServiceTest {

    @Spy
    private StationCatalogService catalogService = new StationCatalogService();

    @Mock
    private SimulatedSocialFeedEngine socialFeedEngine;

    @InjectMocks
    private ScenarioLabService scenarioLabService;

    @BeforeEach
    public void setUp() {
        catalogService.initCatalog();
    }

    @Test
    public void testTriggerMumbaiCloudburst() {
        ScenarioTriggerRequest request = new ScenarioTriggerRequest();
        request.setScenarioType(ScenarioType.MUMBAI_CLOUDBURST);

        Map<String, Object> status = scenarioLabService.triggerScenario(request);

        assertNotNull(status);
        assertEquals("MUMBAI_CLOUDBURST", status.get("activeScenario"));
        // Colaba AWS should have high rainfall
        assertTrue(catalogService.getStation("stn-mum-01").get().getCurrentRainfallMm() >= 80.0);
    }

    @Test
    public void testTriggerOdishaCyclone() {
        ScenarioTriggerRequest request = new ScenarioTriggerRequest();
        request.setScenarioType(ScenarioType.ODISHA_CYCLONE);

        Map<String, Object> status = scenarioLabService.triggerScenario(request);

        assertNotNull(status);
        assertEquals("ODISHA_CYCLONE", status.get("activeScenario"));
        // Puri AWS should have gale winds and low pressure
        assertTrue(catalogService.getStation("stn-odi-02").get().getCurrentWindSpeedKmh() >= 100.0);
        assertTrue(catalogService.getStation("stn-odi-02").get().getCurrentPressure() <= 985.0);
    }

    @Test
    public void testTriggerDelhiHeatwave() {
        ScenarioTriggerRequest request = new ScenarioTriggerRequest();
        request.setScenarioType(ScenarioType.DELHI_HEATWAVE);

        Map<String, Object> status = scenarioLabService.triggerScenario(request);

        assertNotNull(status);
        assertEquals("DELHI_HEATWAVE", status.get("activeScenario"));
        // Palam AWS should have extreme temperature
        assertTrue(catalogService.getStation("stn-del-02").get().getCurrentTemperature() >= 47.0);
    }
}
