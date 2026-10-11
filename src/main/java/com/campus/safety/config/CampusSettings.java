package com.campus.safety.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CampusSettings(
        @DecimalMin("1.0") @DecimalMax("200.0") double speedLimitKmh,
        @DecimalMin("1.0") @DecimalMax("10000.0") double gateDistanceMeters,
        @Min(60) @Max(86400) long transitTtlSeconds,
        @DecimalMin("20.0") @DecimalMax("200.0") double noiseThresholdDb,
        @Min(1) @Max(3600) long noiseSustainedDurationSeconds,
        @Min(1) @Max(60) long noiseMaxSampleGapSeconds) {
}
