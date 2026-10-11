package com.campus.safety.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus")
public class CampusProperties {

    private double speedLimitKmh = 30.0;
    private double gateDistanceMeters = 300.0;
    private Duration transitTtl = Duration.ofMinutes(10);
    private double noiseThresholdDb = 85.0;
    private Duration noiseSustainedDuration = Duration.ofSeconds(3);
    private Duration noiseMaxSampleGap = Duration.ofSeconds(2);

    public double getSpeedLimitKmh() {
        return speedLimitKmh;
    }

    public void setSpeedLimitKmh(double speedLimitKmh) {
        this.speedLimitKmh = speedLimitKmh;
    }

    public double getGateDistanceMeters() {
        return gateDistanceMeters;
    }

    public void setGateDistanceMeters(double gateDistanceMeters) {
        this.gateDistanceMeters = gateDistanceMeters;
    }

    public Duration getTransitTtl() {
        return transitTtl;
    }

    public void setTransitTtl(Duration transitTtl) {
        this.transitTtl = transitTtl;
    }

    public double getNoiseThresholdDb() {
        return noiseThresholdDb;
    }

    public void setNoiseThresholdDb(double noiseThresholdDb) {
        this.noiseThresholdDb = noiseThresholdDb;
    }

    public Duration getNoiseSustainedDuration() {
        return noiseSustainedDuration;
    }

    public void setNoiseSustainedDuration(Duration noiseSustainedDuration) {
        this.noiseSustainedDuration = noiseSustainedDuration;
    }

    public Duration getNoiseMaxSampleGap() {
        return noiseMaxSampleGap;
    }

    public void setNoiseMaxSampleGap(Duration noiseMaxSampleGap) {
        this.noiseMaxSampleGap = noiseMaxSampleGap;
    }
}
