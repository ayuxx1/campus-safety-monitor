package com.campus.safety.transit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transit")
public class TransitController {

    private final TransitService transitService;

    public TransitController(TransitService transitService) {
        this.transitService = transitService;
    }

    @PostMapping("/entry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EntryResponse recordEntry(@Valid @RequestBody EntryRequest request) {
        transitService.recordEntry(request.vehicleNumber());
        return new EntryResponse(request.vehicleNumber().trim().toUpperCase(), "Entry recorded");
    }

    @PostMapping("/{vehicleNumber}/exit")
    public TransitResult recordExit(@PathVariable @NotBlank String vehicleNumber) {
        return transitService.recordExit(vehicleNumber);
    }

    public record EntryRequest(@NotBlank String vehicleNumber) {
    }

    public record EntryResponse(String vehicleNumber, String message) {
    }
}
