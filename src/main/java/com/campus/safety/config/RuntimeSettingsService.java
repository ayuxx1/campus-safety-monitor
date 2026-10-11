package com.campus.safety.config;

import java.time.Duration;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuntimeSettingsService implements ApplicationRunner {

    private final RuntimeSettingsRepository repository;
    private final CampusProperties properties;

    public RuntimeSettingsService(RuntimeSettingsRepository repository, CampusProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        repository.findById(1).ifPresent(settings -> apply(settings.toSettings()));
    }

    @Transactional(readOnly = true)
    public CampusSettings getSettings() {
        return repository.findById(1)
                .map(RuntimeSettings::toSettings)
                .orElseGet(this::currentSettings);
    }

    @Transactional
    public CampusSettings updateSettings(CampusSettings settings) {
        RuntimeSettings persisted = repository.findById(1)
                .orElseGet(() -> new RuntimeSettings(settings));
        persisted.apply(settings);
        repository.save(persisted);
        apply(settings);
        return settings;
    }

    private CampusSettings currentSettings() {
        return new CampusSettings(
                properties.getSpeedLimitKmh(),
                properties.getGateDistanceMeters(),
                properties.getTransitTtl().toSeconds(),
                properties.getNoiseThresholdDb(),
                properties.getNoiseSustainedDuration().toSeconds(),
                properties.getNoiseMaxSampleGap().toSeconds());
    }

    private void apply(CampusSettings settings) {
        properties.setSpeedLimitKmh(settings.speedLimitKmh());
        properties.setGateDistanceMeters(settings.gateDistanceMeters());
        properties.setTransitTtl(Duration.ofSeconds(settings.transitTtlSeconds()));
        properties.setNoiseThresholdDb(settings.noiseThresholdDb());
        properties.setNoiseSustainedDuration(Duration.ofSeconds(settings.noiseSustainedDurationSeconds()));
        properties.setNoiseMaxSampleGap(Duration.ofSeconds(settings.noiseMaxSampleGapSeconds()));
    }
}
