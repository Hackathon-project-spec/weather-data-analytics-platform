package in.gov.moes.sih26069.ingestion;

import in.gov.moes.sih26069.common.dto.AiEventDTO;
import in.gov.moes.sih26069.common.dto.GeoLocation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AiEventValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidAiEventPassesValidation() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId("event-001");
        event.setEventType("FLOOD");
        event.setSeverity("HIGH");
        event.setConfidence(94.0);
        event.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));

        Set<ConstraintViolation<AiEventDTO>> violations = validator.validate(event);
        assertTrue(violations.isEmpty(), "Valid event should have no constraint violations");
    }

    @Test
    public void testMissingEventIdFailsValidation() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId(""); // Blank!
        event.setEventType("FLOOD");
        event.setSeverity("HIGH");
        event.setConfidence(94.0);
        event.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));

        Set<ConstraintViolation<AiEventDTO>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventId")));
    }

    @Test
    public void testInvalidLatitudeFailsValidation() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId("event-002");
        event.setEventType("FLOOD");
        event.setSeverity("HIGH");
        event.setConfidence(94.0);
        event.setLocation(new GeoLocation(105.0, 72.8777, "Mumbai", "Maharashtra")); // Lat > 90!

        Set<ConstraintViolation<AiEventDTO>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("latitude must be between -90 and 90")));
    }

    @Test
    public void testInvalidConfidenceFailsValidation() {
        AiEventDTO event = new AiEventDTO();
        event.setEventId("event-003");
        event.setEventType("FLOOD");
        event.setSeverity("HIGH");
        event.setConfidence(150.0); // Confidence > 100!
        event.setLocation(new GeoLocation(19.0760, 72.8777, "Mumbai", "Maharashtra"));

        Set<ConstraintViolation<AiEventDTO>> violations = validator.validate(event);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("confidence must be between 0 and 100")));
    }
}
