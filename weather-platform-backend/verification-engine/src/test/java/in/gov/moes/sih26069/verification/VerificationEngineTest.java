package in.gov.moes.sih26069.verification;

import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import in.gov.moes.sih26069.common.event.CitizenReportEvent;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import in.gov.moes.sih26069.common.event.VerifiedReportEvent;
import in.gov.moes.sih26069.verification.service.SpatialGroundTruthService;
import in.gov.moes.sih26069.verification.service.VerificationScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VerificationEngineTest {

    @Spy
    private SpatialGroundTruthService groundTruthService = new SpatialGroundTruthService();

    @InjectMocks
    private VerificationScoringEngine scoringEngine;

    @BeforeEach
    public void setUp() {
        groundTruthService.initStations();
    }

    @Test
    public void testHaversineDistance() {
        // Colaba to Santacruz in Mumbai is approx 21km
        double dist = groundTruthService.calculateHaversineDistanceKm(18.8997, 72.8153, 19.0896, 72.8656);
        assertTrue(dist >= 20.0 && dist <= 23.0, "Distance should be ~21km, got " + dist);
    }

    @Test
    public void testMumbaiCloudburstReportVerification() {
        // 1. Inject extreme rainfall into Colaba AWS
        TelemetryEvent extremeRain = new TelemetryEvent();
        extremeRain.setStationId("stn-mum-01");
        extremeRain.setTemperature(26.5);
        extremeRain.setPrecipitationMm(95.0); // 95mm/hr
        extremeRain.setWindSpeedKmh(35.0);
        extremeRain.setPressure(998.0);
        groundTruthService.updateStationFromTelemetry(extremeRain);

        // 2. Submit citizen flood report nearby Colaba (18.91, 72.82)
        CitizenReportEvent report = new CitizenReportEvent();
        report.setReportId("rep-test-flood");
        report.setReporterName("Rohan Mehta");
        report.setCategory(DisasterCategory.FLOOD);
        report.setSeverityLevel(5);
        report.setLatitude(18.9100);
        report.setLongitude(72.8200);
        report.setState("Maharashtra");
        report.setDistrict("Mumbai City");
        report.setDescription("Extreme waterlogging on Shahid Bhagat Singh road, knee deep water!");
        report.setUpvotes(3);
        report.setTimestamp(Instant.now());

        VerifiedReportEvent verified = scoringEngine.evaluateReport(report);

        assertNotNull(verified);
        assertEquals(VerificationStatus.VERIFIED, verified.getStatus());
        assertTrue(verified.getConfidenceScore() >= 80.0, "Confidence score should be >= 80%, was: " + verified.getConfidenceScore());
        assertTrue(verified.getScoreBreakdown().getSensorMatchPoints() >= 35.0, "Sensor match points should be >= 35");
        assertTrue(verified.getStationDistanceKm() < 5.0, "Station distance should be < 5km");
        assertTrue(verified.getLatencyMs() >= 0, "Latency must be tracked");
    }

    @Test
    public void testFakeBlizzardInChennaiDebunking() {
        // 1. Keep Chennai AWS at 34°C with 0mm rain
        TelemetryEvent sunny = new TelemetryEvent();
        sunny.setStationId("stn-chn-02");
        sunny.setTemperature(34.5);
        sunny.setPrecipitationMm(0.0);
        groundTruthService.updateStationFromTelemetry(sunny);

        // 2. Submit fake blizzard report in Chennai
        CitizenReportEvent fakeReport = new CitizenReportEvent();
        fakeReport.setReportId("rep-fake-blizzard");
        fakeReport.setReporterName("Bot Spammer");
        fakeReport.setCategory(DisasterCategory.BLIZZARD);
        fakeReport.setSeverityLevel(4);
        fakeReport.setLatitude(13.0600);
        fakeReport.setLongitude(80.2400);
        fakeReport.setState("Tamil Nadu");
        fakeReport.setDistrict("Chennai");
        fakeReport.setDescription("Snow blizzard hitting Marina Beach!");
        fakeReport.setUpvotes(0);
        fakeReport.setTimestamp(Instant.now());

        VerifiedReportEvent verified = scoringEngine.evaluateReport(fakeReport);

        assertNotNull(verified);
        assertEquals(VerificationStatus.DEBUNKED, verified.getStatus(), "Blizzard claim in 34.5°C Chennai must be DEBUNKED");
        assertTrue(verified.getConfidenceScore() < 40.0, "Score must be < 40%, was: " + verified.getConfidenceScore());
        assertEquals(0.0, verified.getScoreBreakdown().getSensorMatchPoints(), "Sensor match points must be 0 for blizzard refutation");
    }
}
