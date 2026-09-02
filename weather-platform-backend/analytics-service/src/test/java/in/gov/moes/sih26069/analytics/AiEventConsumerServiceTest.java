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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiEventConsumerServiceTest {

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

    @Test
    public void testHighConfidenceMumbaiFloodTriggersActiveAlert() {
        AiEventDTO floodEvent = new AiEventDTO();
        floodEvent.setEventId("event-mumbai-001");
        floodEvent.setEventType("FLOOD");
        floodEvent.setSeverity("HIGH");
        floodEvent.setConfidence(94.0);
        floodEvent.setReportCount(100);
        floodEvent.setSummary("Multiple sources indicate severe flooding in Mumbai.");
        floodEvent.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));

        when(aiEventRepository.existsById("event-mumbai-001")).thenReturn(false);

        consumerService.onAiEventReceived(floodEvent);

        // Verify operational status is ACTIVE_ALERT
        assertEquals(OperationalEventStatus.ACTIVE_ALERT, floodEvent.getOperationalStatus());

        // Verify PostgreSQL persistence for event
        verify(aiEventRepository, times(1)).save(any(AiEventEntity.class));

        // Verify PostgreSQL persistence for alert
        verify(alertRepository, times(1)).save(any(WeatherAlertEntity.class));

        // Verify Redis / in-memory cache update
        verify(alertManagementService, times(1)).cacheActiveAlert(any(WeatherAlertEvent.class));

        // Verify Kafka broadcast to weather.alerts.broadcast
        verify(alertKafkaTemplate, times(1)).send(
                eq(AiEventConsumerService.TOPIC_WEATHER_ALERTS_BROADCAST),
                eq("alt-ai-event-mumbai-001"),
                any(WeatherAlertEvent.class)
        );
    }

    @Test
    public void testLowConfidenceBlizzardPersistedWithoutActiveAlert() {
        AiEventDTO blizzardEvent = new AiEventDTO();
        blizzardEvent.setEventId("event-chennai-blizzard-001");
        blizzardEvent.setEventType("BLIZZARD");
        blizzardEvent.setSeverity("LOW");
        blizzardEvent.setConfidence(12.0);
        blizzardEvent.setReportCount(1);
        blizzardEvent.setSummary("Blizzard claimed in Chennai");
        blizzardEvent.setLocation(new GeoLocation(13.0827, 80.2707, "Chennai", "Tamil Nadu"));

        when(aiEventRepository.existsById("event-chennai-blizzard-001")).thenReturn(false);

        consumerService.onAiEventReceived(blizzardEvent);

        // Verify operational status is DEBUNKED
        assertEquals(OperationalEventStatus.DEBUNKED, blizzardEvent.getOperationalStatus());

        // Verify PostgreSQL persistence for audit/analytics
        verify(aiEventRepository, times(1)).save(any(AiEventEntity.class));

        // Verify NO active alert created in alert repository
        verify(alertRepository, never()).save(any(WeatherAlertEntity.class));

        // Verify NO broadcast alert published to Kafka
        verify(alertKafkaTemplate, never()).send(anyString(), anyString(), any(WeatherAlertEvent.class));
    }

    @Test
    public void testDuplicateEventIdIgnored() {
        AiEventDTO event1 = new AiEventDTO();
        event1.setEventId("event-dup-test");
        event1.setEventType("FLOOD");
        event1.setSeverity("HIGH");
        event1.setConfidence(90.0);
        event1.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));

        when(aiEventRepository.existsById("event-dup-test")).thenReturn(false);

        consumerService.onAiEventReceived(event1);

        // Reset mocks to test second invocation
        clearInvocations(aiEventRepository, alertRepository, alertKafkaTemplate);

        // Second invocation with same eventId
        consumerService.onAiEventReceived(event1);

        // Should NOT save or publish again
        verify(aiEventRepository, never()).save(any(AiEventEntity.class));
        verify(alertRepository, never()).save(any(WeatherAlertEntity.class));
        verify(alertKafkaTemplate, never()).send(anyString(), anyString(), any(WeatherAlertEvent.class));
    }
}
