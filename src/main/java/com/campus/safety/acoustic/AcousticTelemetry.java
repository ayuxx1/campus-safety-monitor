package com.campus.safety.acoustic;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "acoustic_telemetry")
public class AcousticTelemetry {

    @Id
    private String id;

    private String zoneId;
    private double decibelLevel;
    private boolean excessive;
    private boolean sustainedViolation;
    private double sustainedDurationSeconds;
    private Instant recordedAt;

    public AcousticTelemetry() {
    }

    public AcousticTelemetry(String zoneId, double decibelLevel, boolean excessive,
                             boolean sustainedViolation, double sustainedDurationSeconds,
                             Instant recordedAt) {
        this.zoneId = zoneId;
        this.decibelLevel = decibelLevel;
        this.excessive = excessive;
        this.sustainedViolation = sustainedViolation;
        this.sustainedDurationSeconds = sustainedDurationSeconds;
        this.recordedAt = recordedAt;
    }

    public String getId() {
        return id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public double getDecibelLevel() {
        return decibelLevel;
    }

    public boolean isExcessive() {
        return excessive;
    }

    public boolean isSustainedViolation() {
        return sustainedViolation;
    }

    public double getSustainedDurationSeconds() {
        return sustainedDurationSeconds;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
