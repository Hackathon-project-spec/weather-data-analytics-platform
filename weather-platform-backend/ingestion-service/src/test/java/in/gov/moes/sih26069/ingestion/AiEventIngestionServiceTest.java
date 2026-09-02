package in.gov.moes.sih26069.ingestion;

import in.gov.moes.sih26069.common.dto.AiEventDTO;
import in.gov.moes.sih26069.common.dto.GeoLocation;
import in.gov.moes.sih26069.ingestion.service.AiEventIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiEventIngestionServiceTest {

    @Mock
    private KafkaTemplate<String, AiEventDTO> aiEventKafkaTemplate;

    @InjectMocks
    private AiEventIngestionService ingestionService;

    private AiEventDTO createValidFloodEvent(String eventId) {
        AiEventDTO event = new AiEventDTO();
        event.setEventId(eventId);
        event.setEventType("FLOOD");
        event.setSource("AI_ANALYSIS");
        event.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));
        event.setSeverity("HIGH");
        event.setConfidence(94.0);
        event.setReportCount(100);
        event.setSummary("Multiple sources indicate severe flooding in Mumbai.");
        return event;
    }

    @Test
    public void testIngestValidAiEvent() {
        AiEventDTO event = createValidFloodEvent("event-test-001");

        Map<String, Object> result = ingestionService.ingestAiEvent(event);

        assertNotNull(result);
        assertEquals("INGESTED", result.get("status"));
        assertEquals("event-test-001", result.get("eventId"));
        verify(aiEventKafkaTemplate, times(1)).send(eq(AiEventIngestionService.TOPIC_WEATHER_AI_EVENTS), eq("event-test-001"), eq(event));
    }

    @Test
    public void testIdempotentDuplicateEventIngestion() {
        AiEventDTO event1 = createValidFloodEvent("event-duplicate-001");
        AiEventDTO event2 = createValidFloodEvent("event-duplicate-001");

        Map<String, Object> result1 = ingestionService.ingestAiEvent(event1);
        assertEquals("INGESTED", result1.get("status"));

        // Second duplicate ingestion
        Map<String, Object> result2 = ingestionService.ingestAiEvent(event2);
        assertEquals("DUPLICATE_ACCEPTED", result2.get("status"));
        assertTrue(result2.get("message").toString().contains("already received and processed"));

        // Kafka should have been called only once!
        verify(aiEventKafkaTemplate, times(1)).send(eq(AiEventIngestionService.TOPIC_WEATHER_AI_EVENTS), eq("event-duplicate-001"), any(AiEventDTO.class));
    }

    @Test
    public void testLowConfidenceBlizzardEventAcceptedAndPublished() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId("event-blizzard-001");
        event.setEventType("BLIZZARD");
        event.setSource("AI_ANALYSIS");
        event.setLocation(new GeoLocation(13.0827, 80.2707, "Chennai", "Tamil Nadu"));
        event.setSeverity("LOW");
        event.setConfidence(12.0);
        event.setReportCount(1);
        event.setSummary("Blizzard claimed in Chennai");

        Map<String, Object> result = ingestionService.ingestAiEvent(event);

        assertEquals("INGESTED", result.get("status"));
        verify(aiEventKafkaTemplate, times(1)).send(eq(AiEventIngestionService.TOPIC_WEATHER_AI_EVENTS), eq("event-blizzard-001"), eq(event));
    }
}
