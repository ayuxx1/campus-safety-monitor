package com.campus.safety.acoustic;

import com.campus.safety.config.CampusProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AcousticMonitorService {

    private static final String KEY_PREFIX = "acoustic:zone:";

    private final AcousticTelemetryRepository telemetryRepository;
    private final StringRedisTemplate redisTemplate;
    private final CampusProperties properties;
    private final Clock clock;

    public AcousticMonitorService(AcousticTelemetryRepository telemetryRepository,
                                  StringRedisTemplate redisTemplate,
                                  CampusProperties properties,
                                  Clock clock) {
        this.telemetryRepository = telemetryRepository;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    public AcousticReadingResult processReading(String zoneId, double decibelLevel) {
        String normalizedZoneId = zoneId.trim().toUpperCase(Locale.ROOT);
        String baseKey = KEY_PREFIX + normalizedZoneId;
        String startKey = baseKey + ":highSince";
        String lastKey = baseKey + ":lastHigh";
        String reportedKey = baseKey + ":reported";
        long nowMillis = clock.millis();
        boolean excessive = decibelLevel > properties.getNoiseThresholdDb();
        boolean sustainedViolation = false;
        double sustainedDurationSeconds = 0.0;

        if (excessive) {
            String startValue = redisTemplate.opsForValue().get(startKey);
            String lastValue = redisTemplate.opsForValue().get(lastKey);
            if (startValue == null || lastValue == null
                    || nowMillis - Long.parseLong(lastValue) > properties.getNoiseMaxSampleGap().toMillis()) {
                startValue = Long.toString(nowMillis);
                redisTemplate.opsForValue().set(startKey, startValue, properties.getTransitTtl());
                redisTemplate.delete(reportedKey);
            }

            long startMillis = Long.parseLong(startValue);
            sustainedDurationSeconds = Math.max(0, nowMillis - startMillis) / 1000.0;
            redisTemplate.opsForValue().set(startKey, startValue, properties.getTransitTtl());
            redisTemplate.opsForValue().set(lastKey, Long.toString(nowMillis), properties.getTransitTtl());

            if (nowMillis - startMillis > properties.getNoiseSustainedDuration().toMillis()) {
                Boolean firstViolation = redisTemplate.opsForValue().setIfAbsent(
                        reportedKey, "true", properties.getTransitTtl());
                sustainedViolation = Boolean.TRUE.equals(firstViolation);
                if (Boolean.FALSE.equals(firstViolation)) {
                    redisTemplate.opsForValue().set(reportedKey, "true", properties.getTransitTtl());
                }
            }
        } else {
            redisTemplate.delete(startKey);
            redisTemplate.delete(lastKey);
            redisTemplate.delete(reportedKey);
        }

        Instant recordedAt = Instant.now(clock);
        telemetryRepository.save(new AcousticTelemetry(
                normalizedZoneId,
                decibelLevel,
                excessive,
                sustainedViolation,
                sustainedDurationSeconds,
                recordedAt));

        return new AcousticReadingResult(
                normalizedZoneId,
                decibelLevel,
                excessive,
                sustainedViolation,
                sustainedDurationSeconds,
                recordedAt);
    }
}
