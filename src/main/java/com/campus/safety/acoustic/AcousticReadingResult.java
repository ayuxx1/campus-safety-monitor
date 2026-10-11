package com.campus.safety.acoustic;

import java.time.Instant;

public record AcousticReadingResult(
        String zoneId,
        double decibelLevel,
        boolean excessive,
        boolean sustainedViolation,
        double sustainedDurationSeconds,
        Instant recordedAt) {
}
