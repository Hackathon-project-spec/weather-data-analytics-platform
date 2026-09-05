package in.gov.moes.sih26069.analytics;

import in.gov.moes.sih26069.analytics.controller.AnalyticsController;
import in.gov.moes.sih26069.analytics.entity.AiEventEntity;
import in.gov.moes.sih26069.analytics.repository.AiEventRepository;
import in.gov.moes.sih26069.analytics.service.AlertManagementService;
import in.gov.moes.sih26069.analytics.service.ClickHouseTimeSeriesService;
import in.gov.moes.sih26069.common.dto.SystemStatsDTO;
import in.gov.moes.sih26069.common.enums.AlertSeverity;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import in.gov.moes.sih26069.common.event.WeatherAlertEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerTest {

    @Mock
    private ClickHouseTimeSeriesService timeSeriesService;

    @Mock
    private AlertManagementService alertService;

    @Mock
    private AiEventRepository aiEventRepository;

    @InjectMocks
    private AnalyticsController controller;

    @Test
    public void testGetEventsRetrievesFromRepository() {
        AiEventEntity entity = new AiEventEntity();
        entity.setId("test-event-001");
        entity.setEventType("FLOOD");
        entity.setSeverity("HIGH");
        entity.setOperationalStatus(OperationalEventStatus.ACTIVE_ALERT);

        when(aiEventRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        ResponseEntity<List<AiEventEntity>> response = controller.getEvents(null, null, null, null, null, 50);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("test-event-001", response.getBody().get(0).getId());
    }

    @Test
    public void testGetEventByIdFoundAndNotFound() {
        AiEventEntity entity = new AiEventEntity();
        entity.setId("test-event-002");
        when(aiEventRepository.findById("test-event-002")).thenReturn(Optional.of(entity));
        when(aiEventRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseEntity<AiEventEntity> found = controller.getEventById("test-event-002");
        assertEquals(200, found.getStatusCode().value());
        assertNotNull(found.getBody());
        assertEquals("test-event-002", found.getBody().getId());

        ResponseEntity<AiEventEntity> notFound = controller.getEventById("unknown");
        assertEquals(404, notFound.getStatusCode().value());
    }

    @Test
    public void testGetAlertsRetrievesFromAlertService() {
        WeatherAlertEvent alert = new WeatherAlertEvent();
        alert.setAlertId("alt-001");
        alert.setSeverity(AlertSeverity.EXTREME);
        alert.setCategory(DisasterCategory.FLOOD);

        when(alertService.getAllAlerts(any(), any(), any(), any())).thenReturn(List.of(alert));

        ResponseEntity<List<WeatherAlertEvent>> response = controller.getAllAlerts("EXTREME", null, null, null);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("alt-001", response.getBody().get(0).getAlertId());
    }

    @Test
    public void testGetSystemStatsDynamicCounts() {
        when(timeSeriesService.getTotalTelemetryCount()).thenReturn(500L);
        when(alertService.getActiveAlerts()).thenReturn(List.of(new WeatherAlertEvent()));
        when(timeSeriesService.getTotalAiEventsRecorded()).thenReturn(20L);

        ResponseEntity<SystemStatsDTO> response = controller.getSystemStats();
        assertEquals(200, response.getStatusCode().value());
        SystemStatsDTO stats = response.getBody();
        assertNotNull(stats);
        assertEquals(500L, stats.getTotalTelemetryCount());
        assertEquals(1, stats.getActiveAlertsCount());
        assertEquals(30, stats.getActiveStationsCount());
    }
}
