package com.campus.safety.transit;

public record TransitResult(
        String vehicleNumber,
        double speedKmh,
        boolean violated,
        String message,
        Long violationId) {
}
