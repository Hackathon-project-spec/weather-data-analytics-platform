package in.gov.moes.sih26069.citizen;

import in.gov.moes.sih26069.citizen.entity.CitizenReportEntity;
import in.gov.moes.sih26069.citizen.repository.CitizenReportRepository;
import in.gov.moes.sih26069.citizen.service.CitizenReportService;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import in.gov.moes.sih26069.common.event.CitizenReportEvent;
import in.gov.moes.sih26069.common.event.VerifiedReportEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CitizenReportServiceTest {

    @Mock
    private CitizenReportRepository repository;

    @InjectMocks
    private CitizenReportService reportService;

    @Test
    public void testSubmitReport() {
        CitizenReportEvent event = new CitizenReportEvent();
        event.setReporterName("Aarav Sharma");
        event.setCategory(DisasterCategory.FLOOD);
        event.setSeverityLevel(4);
        event.setLatitude(19.0760);
        event.setLongitude(72.8777);
        event.setState("Maharashtra");
        event.setDistrict("Mumbai City");
        event.setDescription("Heavy water buildup near railway bridge");

        when(repository.save(any(CitizenReportEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitizenReportEntity saved = reportService.submitReport(event);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Aarav Sharma", saved.getReporterName());
        assertEquals(DisasterCategory.FLOOD, saved.getCategory());
        assertEquals(VerificationStatus.PENDING, saved.getVerificationStatus());
        verify(repository, times(1)).save(any(CitizenReportEntity.class));
    }

    @Test
    public void testOnVerifiedReportReceived() {
        CitizenReportEntity entity = new CitizenReportEntity();
        entity.setId("rep-12345");
        entity.setVerificationStatus(VerificationStatus.PENDING);

        when(repository.findById("rep-12345")).thenReturn(Optional.of(entity));

        VerifiedReportEvent verifiedEvent = new VerifiedReportEvent();
        verifiedEvent.setReportId("rep-12345");
        verifiedEvent.setStatus(VerificationStatus.VERIFIED);
        verifiedEvent.setConfidenceScore(94.5);
        verifiedEvent.setReasoning("Confirmed by Colaba AWS 95mm/hr");
        verifiedEvent.setMatchedStationId("stn-mum-01");
        verifiedEvent.setStationDistanceKm(3.2);
        verifiedEvent.setLatencyMs(45);

        reportService.onVerifiedReportReceived(verifiedEvent);

        assertEquals(VerificationStatus.VERIFIED, entity.getVerificationStatus());
        assertEquals(94.5, entity.getConfidenceScore());
        assertEquals(45, entity.getVerificationLatencyMs());
        verify(repository, times(1)).save(entity);
    }

    @Test
    public void testDuplicateReportSubmissionReturnsExisting() {
        CitizenReportEntity existing = new CitizenReportEntity();
        existing.setId("rep-existing-123");
        existing.setReporterName("Original Citizen");
        existing.setCategory(DisasterCategory.FLOOD);

        when(repository.findById("rep-existing-123")).thenReturn(Optional.of(existing));

        CitizenReportEvent duplicateEvent = new CitizenReportEvent();
        duplicateEvent.setReportId("rep-existing-123");
        duplicateEvent.setReporterName("Duplicate Submission");
        duplicateEvent.setCategory(DisasterCategory.FLOOD);

        CitizenReportEntity result = reportService.submitReport(duplicateEvent);

        assertEquals("rep-existing-123", result.getId());
        assertEquals("Original Citizen", result.getReporterName());
        // Verify save was NOT called for duplicate!
        verify(repository, never()).save(any(CitizenReportEntity.class));
    }
}
