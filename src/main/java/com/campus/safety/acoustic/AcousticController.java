package com.campus.safety.acoustic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acoustics")
public class AcousticController {

    private final AcousticMonitorService acousticMonitorService;

    public AcousticController(AcousticMonitorService acousticMonitorService) {
        this.acousticMonitorService = acousticMonitorService;
    }

    @PostMapping("/readings")
    @ResponseStatus(HttpStatus.CREATED)
    public AcousticReadingResult ingestReading(@Valid @RequestBody AcousticReadingRequest request) {
        return acousticMonitorService.processReading(request.zoneId(), request.decibelLevel());
    }

    public record AcousticReadingRequest(
            @NotBlank String zoneId,
            @DecimalMin("0.0") @DecimalMax("200.0") double decibelLevel) {
    }
}
