package com.campus.safety.acoustic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.campus.safety.config.CampusProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class AcousticMonitorServiceTest {

    private final AcousticTelemetryRepository telemetryRepository = mock(AcousticTelemetryRepository.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final Map<String, String> redisState = new HashMap<>();
    private final CampusProperties properties = new CampusProperties();
    private final Clock baseClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenAnswer(invocation ->
                redisState.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            redisState.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenAnswer(invocation -> redisState.putIfAbsent(
                        invocation.getArgument(0), invocation.getArgument(1)) == null);
        when(redisTemplate.delete(anyString())).thenAnswer(invocation ->
                redisState.remove(invocation.getArgument(0)) != null);
        when(telemetryRepository.save(any(AcousticTelemetry.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
    }

    @Test
    void reportsOnceWhenHighNoiseContinuesLongerThanThreeSeconds() {
        processReadingAt(0);
        processReadingAt(2_000);

        AcousticReadingResult thresholdReading = processReadingAt(3_100);
        AcousticReadingResult followingReading = processReadingAt(4_000);

        assertThat(thresholdReading.sustainedViolation()).isTrue();
        assertThat(thresholdReading.sustainedDurationSeconds()).isEqualTo(3.1);
        assertThat(followingReading.sustainedViolation()).isFalse();
    }

    @Test
    void startsANewNoiseWindowAfterAnExcessiveSampleGap() {
        processReadingAt(0);

        AcousticReadingResult afterGap = processReadingAt(2_100);

        assertThat(afterGap.sustainedViolation()).isFalse();
        assertThat(afterGap.sustainedDurationSeconds()).isZero();
    }

    private AcousticReadingResult processReadingAt(long millis) {
        AcousticMonitorService service = new AcousticMonitorService(
                telemetryRepository,
                redisTemplate,
                properties,
                Clock.offset(baseClock, Duration.ofMillis(millis)));
        return service.processReading("library-west", 86.0);
    }
}
