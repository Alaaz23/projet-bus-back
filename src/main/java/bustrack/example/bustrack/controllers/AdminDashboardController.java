package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Feedback;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.models.TrajetHistory;
import bustrack.example.bustrack.repositories.FeedbackRepository;
import bustrack.example.bustrack.repositories.SalarieRepository;
import bustrack.example.bustrack.repositories.TrajetHistoryRepository;
import bustrack.example.bustrack.services.PdfExportService;
import bustrack.example.bustrack.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private StatisticsService statisticsService;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private SalarieRepository salarieRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private TrajetHistoryRepository trajetHistoryRepository;

    // ─── Statistics ────────────────────────────────────────────────────────────

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(statisticsService.getGlobalStats());
    }

    // ─── PDF Export ────────────────────────────────────────────────────────────

    @GetMapping("/export/salaries")
    public ResponseEntity<byte[]> exportSalaries() {
        List<Salarie> salaries = salarieRepository.findAll();
        byte[] pdf = pdfExportService.exportSalaries(salaries);
        String filename = "salaries_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/feedbacks")
    public ResponseEntity<byte[]> exportFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        byte[] pdf = pdfExportService.exportFeedbacks(feedbacks);
        String filename = "feedbacks_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/trajets")
    public ResponseEntity<byte[]> exportTrajets() {
        List<TrajetHistory> trajets = trajetHistoryRepository.findAllByOrderByDateDepartDesc(PageRequest.of(0, 100));
        byte[] pdf = pdfExportService.exportTrajetHistory(trajets);
        String filename = "trajets_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
