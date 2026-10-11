package com.campus.safety.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RuntimeSettings {

    @Id
    private Integer id = 1;

    private double speedLimitKmh;
    private double gateDistanceMeters;
    private long transitTtlSeconds;
    private double noiseThresholdDb;
    private long noiseSustainedDurationSeconds;
    private long noiseMaxSampleGapSeconds;

    protected RuntimeSettings() {
    }

    RuntimeSettings(CampusSettings settings) {
        apply(settings);
    }

    void apply(CampusSettings settings) {
        speedLimitKmh = settings.speedLimitKmh();
        gateDistanceMeters = settings.gateDistanceMeters();
        transitTtlSeconds = settings.transitTtlSeconds();
        noiseThresholdDb = settings.noiseThresholdDb();
        noiseSustainedDurationSeconds = settings.noiseSustainedDurationSeconds();
        noiseMaxSampleGapSeconds = settings.noiseMaxSampleGapSeconds();
    }

    CampusSettings toSettings() {
        return new CampusSettings(speedLimitKmh, gateDistanceMeters, transitTtlSeconds,
                noiseThresholdDb, noiseSustainedDurationSeconds, noiseMaxSampleGapSeconds);
    }

    public Integer getId() {
        return id;
    }
}
