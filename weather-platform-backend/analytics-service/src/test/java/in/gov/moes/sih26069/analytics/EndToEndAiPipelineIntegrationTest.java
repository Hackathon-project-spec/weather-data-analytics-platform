package in.gov.moes.sih26069.analytics;

import in.gov.moes.sih26069.analytics.entity.AiEventEntity;
import in.gov.moes.sih26069.analytics.entity.WeatherAlertEntity;
import in.gov.moes.sih26069.analytics.repository.AiEventRepository;
import in.gov.moes.sih26069.analytics.repository.WeatherAlertRepository;
import in.gov.moes.sih26069.analytics.service.AiEventConsumerService;
import in.gov.moes.sih26069.analytics.service.AlertManagementService;
import in.gov.moes.sih26069.analytics.service.ClickHouseTimeSeriesService;
import in.gov.moes.sih26069.common.dto.AiEventDTO;
import in.gov.moes.sih26069.common.dto.GeoLocation;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EndToEndAiPipelineIntegrationTest {

    @Mock
    private AiEventRepository aiEventRepository;

    @Mock
    private WeatherAlertRepository alertRepository;

    @Mock
    private AlertManagementService alertManagementService;

    @Mock
    private ClickHouseTimeSeriesService clickHouseService;

    @Mock
    private KafkaTemplate<String, WeatherAlertEvent> alertKafkaTemplate;

    @InjectMocks
    private AiEventConsumerService consumerService;

    @BeforeEach
    public void setUp() {
        // Reset before each test
    }

    @Test
    @DisplayName("End-to-End Pipeline: Mumbai Flood Scenario (Confidence 94%, High Severity) -> Active Alert Broadcast")
    public void testMumbaiFloodEndToEndPipeline() {
        // 1. AI sends structured Mumbai Flood event
        AiEventDTO mumbaiFlood = new AiEventDTO();
        mumbaiFlood.setEventId("event-001");
        mumbaiFlood.setEventType("FLOOD");
        mumbaiFlood.setSource("AI_ANALYSIS");
        mumbaiFlood.setLocation(new GeoLocation(19.076, 72.877, "Mumbai", "Maharashtra"));
        mumbaiFlood.setSeverity("HIGH");
        mumbaiFlood.setConfidence(94.0);
        mumbaiFlood.setReportCount(100);
        mumbaiFlood.setSummary("Multiple sources indicate severe flooding.");

        when(aiEventRepository.existsById("event-001")).thenReturn(false);

        // 2. Consumer processes event
        consumerService.onAiEventReceived(mumbaiFlood);

        // 3. Verify Operational Decision
        assertEquals(OperationalEventStatus.ACTIVE_ALERT, mumbaiFlood.getOperationalStatus());

        // 4. Verify PostgreSQL persistence for AI Event
        ArgumentCaptor<AiEventEntity> eventCaptor = ArgumentCaptor.forClass(AiEventEntity.class);
        verify(aiEventRepository, times(1)).save(eventCaptor.capture());
        AiEventEntity savedEvent = eventCaptor.getValue();
        assertEquals("event-001", savedEvent.getId());
        assertEquals("FLOOD", savedEvent.getEventType());
        assertEquals("Mumbai", savedEvent.getCity());
        assertEquals(94.0, savedEvent.getConfidence());
        assertEquals(OperationalEventStatus.ACTIVE_ALERT, savedEvent.getOperationalStatus());

        // 5. Verify PostgreSQL persistence for Active Weather Alert
        ArgumentCaptor<WeatherAlertEntity> alertCaptor = ArgumentCaptor.forClass(WeatherAlertEntity.class);
        verify(alertRepository, times(1)).save(alertCaptor.capture());
        WeatherAlertEntity savedAlert = alertCaptor.getValue();
        assertEquals("alt-ai-event-001", savedAlert.getId());
        assertTrue(savedAlert.getHeadline().contains("Mumbai"));
        assertTrue(savedAlert.getIsActive());

        // 6. Verify ClickHouse Analytical recording
        verify(clickHouseService, times(1)).recordAiEventAnalytics(eq(mumbaiFlood), eq(OperationalEventStatus.ACTIVE_ALERT));

        // 7. Verify Redis Active Alert cache updated
        verify(alertManagementService, times(1)).cacheActiveAlert(any(WeatherAlertEvent.class));

        // 8. Verify Kafka Alert published to weather.alerts.broadcast
        ArgumentCaptor<WeatherAlertEvent> broadcastCaptor = ArgumentCaptor.forClass(WeatherAlertEvent.class);
        verify(alertKafkaTemplate, times(1)).send(
                eq(AiEventConsumerService.TOPIC_WEATHER_ALERTS_BROADCAST),
                eq("alt-ai-event-001"),
                broadcastCaptor.capture()
        );
        WeatherAlertEvent broadcastAlert = broadcastCaptor.getValue();
        assertEquals("alt-ai-event-001", broadcastAlert.getAlertId());
        assertTrue(broadcastAlert.getHeadline().contains("FLOOD"));
    }

    @Test
    @DisplayName("End-to-End Pipeline: Fake/Low-Confidence Blizzard in Chennai (Confidence 12%) -> Stored for Analysis, NO Active Alert")
    public void testChennaiBlizzardLowConfidencePipeline() {
        // 1. AI sends unverified/low-confidence Blizzard claim
        AiEventDTO chennaiBlizzard = new AiEventDTO();
        chennaiBlizzard.setEventId("event-fake-002");
        chennaiBlizzard.setEventType("BLIZZARD");
        chennaiBlizzard.setSource("AI_ANALYSIS");
        chennaiBlizzard.setLocation(new GeoLocation(13.0827, 80.2707, "Chennai", "Tamil Nadu"));
        chennaiBlizzard.setSeverity("LOW");
        chennaiBlizzard.setConfidence(12.0);
        chennaiBlizzard.setReportCount(1);
        chennaiBlizzard.setSummary("Blizzard claimed in Chennai");

        when(aiEventRepository.existsById("event-fake-002")).thenReturn(false);

        // 2. Consumer processes event
        consumerService.onAiEventReceived(chennaiBlizzard);

        // 3. Operational decision should classify as DEBUNKED
        assertEquals(OperationalEventStatus.DEBUNKED, chennaiBlizzard.getOperationalStatus());

        // 4. Stored in PostgreSQL for analytical and audit purposes
        ArgumentCaptor<AiEventEntity> eventCaptor = ArgumentCaptor.forClass(AiEventEntity.class);
        verify(aiEventRepository, times(1)).save(eventCaptor.capture());
        assertEquals("event-fake-002", eventCaptor.getValue().getId());
        assertEquals(OperationalEventStatus.DEBUNKED, eventCaptor.getValue().getOperationalStatus());

        // 5. Must NOT create high-priority active alert in PostgreSQL
        verify(alertRepository, never()).save(any(WeatherAlertEntity.class));

        // 6. Must NOT broadcast public alert
        verify(alertKafkaTemplate, never()).send(anyString(), anyString(), any(WeatherAlertEvent.class));
        verify(alertManagementService, never()).cacheActiveAlert(any(WeatherAlertEvent.class));
    }

    @Test
    @DisplayName("End-to-End Pipeline: Duplicate AI Event Prevention")
    public void testDuplicateAiEventPrevention() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId("event-idempotency-001");
        event.setEventType("FLOOD");
        event.setSeverity("HIGH");
        event.setConfidence(92.0);
        event.setLocation(new GeoLocation(19.076, 72.877, "Mumbai", "Maharashtra"));

        when(aiEventRepository.existsById("event-idempotency-001")).thenReturn(false);

        // First ingestion
        consumerService.onAiEventReceived(event);
        verify(aiEventRepository, times(1)).save(any(AiEventEntity.class));
        verify(alertRepository, times(1)).save(any(WeatherAlertEntity.class));

        // Second delivery of identical eventId
        consumerService.onAiEventReceived(event);

        // Save and alert generation counts must still be exactly 1!
        verify(aiEventRepository, times(1)).save(any(AiEventEntity.class));
        verify(alertRepository, times(1)).save(any(WeatherAlertEntity.class));
        verify(alertKafkaTemplate, times(1)).send(anyString(), anyString(), any(WeatherAlertEvent.class));
    }
}
