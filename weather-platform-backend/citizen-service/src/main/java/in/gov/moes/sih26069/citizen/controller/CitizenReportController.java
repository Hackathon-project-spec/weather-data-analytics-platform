package in.gov.moes.sih26069.citizen.controller;

import in.gov.moes.sih26069.citizen.entity.CitizenReportEntity;
import in.gov.moes.sih26069.citizen.service.CitizenReportService;
import in.gov.moes.sih26069.common.event.CitizenReportEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class CitizenReportController {

    @Autowired
    private CitizenReportService reportService;

    @PostMapping
    public ResponseEntity<CitizenReportEntity> submitReport(@RequestBody CitizenReportEvent event) {
        CitizenReportEntity created = reportService.submitReport(event);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CitizenReportEntity>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(reportService.getAllReports(status, state));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitizenReportEntity> getReportById(@PathVariable String id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<CitizenReportEntity> upvoteReport(@PathVariable String id) {
        return reportService.upvoteReport(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
