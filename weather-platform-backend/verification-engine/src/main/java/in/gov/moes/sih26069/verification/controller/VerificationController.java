package in.gov.moes.sih26069.verification.controller;

import in.gov.moes.sih26069.common.event.CitizenReportEvent;
import in.gov.moes.sih26069.common.event.VerifiedReportEvent;
import in.gov.moes.sih26069.verification.service.VerificationScoringEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/verify")
@CrossOrigin(origins = "*")
public class VerificationController {

    @Autowired
    private VerificationScoringEngine scoringEngine;

    @PostMapping("/evaluate")
    public ResponseEntity<VerifiedReportEvent> evaluateReportDirectly(@RequestBody CitizenReportEvent report) {
        VerifiedReportEvent verified = scoringEngine.evaluateReport(report);
        return ResponseEntity.ok(verified);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getVerificationMetrics() {
        return ResponseEntity.ok(scoringEngine.getVerificationMetrics());
    }
}
