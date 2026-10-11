package com.campus.safety.report;

import com.campus.safety.acoustic.AcousticTelemetry;
import com.campus.safety.acoustic.AcousticTelemetryRepository;
import com.campus.safety.transit.SpeedViolation;
import com.campus.safety.transit.SpeedViolationRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final int RECENT_REPORT_LIMIT = 500;

    private final SpeedViolationRepository speedViolations;
    private final AcousticTelemetryRepository acousticTelemetry;

    public ReportController(SpeedViolationRepository speedViolations,
                            AcousticTelemetryRepository acousticTelemetry) {
        this.speedViolations = speedViolations;
        this.acousticTelemetry = acousticTelemetry;
    }

    @GetMapping("/summary")
    public ReportSummary summary() {
        return new ReportSummary(
                speedViolations.count(),
                acousticTelemetry.count(),
                acousticTelemetry.countBySustainedViolationTrue());
    }

    @GetMapping("/speed-violations")
    public List<SpeedViolation> speedViolations() {
        return speedViolations.findAllByOrderByOccurredAtDesc(PageRequest.of(0, RECENT_REPORT_LIMIT));
    }

    @GetMapping("/acoustic-readings")
    public List<AcousticTelemetry> acousticReadings() {
        return acousticTelemetry.findAllByOrderByRecordedAtDesc(PageRequest.of(0, RECENT_REPORT_LIMIT));
    }

    public record ReportSummary(long speedViolationCount, long acousticReadingCount,
                                long sustainedNoiseViolationCount) {
    }
}
