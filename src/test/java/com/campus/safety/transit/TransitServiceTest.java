package com.campus.safety.transit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campus.safety.config.CampusProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class TransitServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final SpeedViolationRepository violationRepository = mock(SpeedViolationRepository.class);
    private final CampusProperties properties = new CampusProperties();
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:20Z"), ZoneOffset.UTC);
    private TransitService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new TransitService(redisTemplate, violationRepository, properties, clock);
    }

    @Test
    void calculatesSpeedAndRecordsSpeedingViolation() {
        when(valueOperations.get("transit:entry:ABC123")).thenReturn("1767225600000");
        when(violationRepository.save(any(SpeedViolation.class))).thenAnswer(invocation -> {
            SpeedViolation violation = invocation.getArgument(0);
            return violation;
        });

        TransitResult result = service.recordExit("abc123");

        assertThat(result.speedKmh()).isEqualTo(54.0);
        assertThat(result.violated()).isTrue();
        assertThat(result.message()).isEqualTo("Over-speeding violation recorded");
        verify(violationRepository).save(any(SpeedViolation.class));
        verify(redisTemplate).delete("transit:entry:ABC123");
    }

    @Test
    void speedAtTheLimitIsNotConsideredAViolation() {
        when(valueOperations.get("transit:entry:ABC123")).thenReturn("1767225584000");

        TransitResult result = service.recordExit("ABC123");

        assertThat(result.speedKmh()).isEqualTo(30.0);
        assertThat(result.violated()).isFalse();
        assertThat(result.violationId()).isNull();
    }
}
