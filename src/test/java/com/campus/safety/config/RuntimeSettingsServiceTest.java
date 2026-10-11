package com.campus.safety.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuntimeSettingsServiceTest {

    private final RuntimeSettingsRepository repository = mock(RuntimeSettingsRepository.class);
    private final CampusProperties properties = new CampusProperties();
    private final RuntimeSettingsService service = new RuntimeSettingsService(repository, properties);

    @Test
    void savesAndAppliesUpdatedThresholdsImmediately() {
        CampusSettings updated = new CampusSettings(40, 450, 900, 90, 5, 3);
        when(repository.findById(1)).thenReturn(Optional.empty());
        when(repository.save(any(RuntimeSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.updateSettings(updated)).isEqualTo(updated);
        assertThat(properties.getSpeedLimitKmh()).isEqualTo(40);
        assertThat(properties.getGateDistanceMeters()).isEqualTo(450);
        assertThat(properties.getTransitTtl()).isEqualTo(Duration.ofSeconds(900));
        assertThat(properties.getNoiseThresholdDb()).isEqualTo(90);
        assertThat(properties.getNoiseSustainedDuration()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getNoiseMaxSampleGap()).isEqualTo(Duration.ofSeconds(3));
    }
}
