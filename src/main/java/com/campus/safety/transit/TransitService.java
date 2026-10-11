package com.campus.safety.transit;

import com.campus.safety.config.CampusProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransitService {

    private static final String ENTRY_KEY_PREFIX = "transit:entry:";

    private final StringRedisTemplate redisTemplate;
    private final SpeedViolationRepository violationRepository;
    private final CampusProperties properties;
    private final Clock clock;

    public TransitService(StringRedisTemplate redisTemplate,
                          SpeedViolationRepository violationRepository,
                          CampusProperties properties,
                          Clock clock) {
        this.redisTemplate = redisTemplate;
        this.violationRepository = violationRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public void recordEntry(String vehicleNumber) {
        String normalizedVehicleNumber = normalizeVehicleNumber(vehicleNumber);
        redisTemplate.opsForValue().set(
                entryKey(normalizedVehicleNumber),
                Long.toString(clock.millis()),
                properties.getTransitTtl());
    }

    public TransitResult recordExit(String vehicleNumber) {
        String normalizedVehicleNumber = normalizeVehicleNumber(vehicleNumber);
        String key = entryKey(normalizedVehicleNumber);
        String entryTimeValue = redisTemplate.opsForValue().get(key);
        if (entryTimeValue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active entry record found");
        }

        long entryTimeMillis;
        try {
            entryTimeMillis = Long.parseLong(entryTimeValue);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Transit entry state is invalid", exception);
        }

        long elapsedMillis = clock.millis() - entryTimeMillis;
        if (elapsedMillis <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transit duration must be greater than zero");
        }

        double elapsedSeconds = elapsedMillis / 1000.0;
        double speedKmh = properties.getGateDistanceMeters() / elapsedSeconds * 3.6;
        boolean violated = speedKmh - properties.getSpeedLimitKmh() > 1.0e-9;

        SpeedViolation violation = violated
                ? violationRepository.save(new SpeedViolation(
                        normalizedVehicleNumber,
                        speedKmh,
                        properties.getSpeedLimitKmh(),
                        Instant.now(clock)))
                : null;

        redisTemplate.delete(key);
        return new TransitResult(
                normalizedVehicleNumber,
                Math.round(speedKmh * 10.0) / 10.0,
                violated,
                violated ? "Over-speeding violation recorded" : "Safe speed",
                violation == null ? null : violation.getId());
    }

    private String entryKey(String vehicleNumber) {
        return ENTRY_KEY_PREFIX + vehicleNumber;
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber.trim().toUpperCase(Locale.ROOT);
    }
}
