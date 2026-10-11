package com.campus.safety.transit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class SpeedViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleNumber;
    private double measuredSpeedKmh;
    private double speedLimitKmh;
    private Instant occurredAt;

    protected SpeedViolation() {
    }

    public SpeedViolation(String vehicleNumber, double measuredSpeedKmh, double speedLimitKmh,
                          Instant occurredAt) {
        this.vehicleNumber = vehicleNumber;
        this.measuredSpeedKmh = measuredSpeedKmh;
        this.speedLimitKmh = speedLimitKmh;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public double getMeasuredSpeedKmh() {
        return measuredSpeedKmh;
    }

    public double getSpeedLimitKmh() {
        return speedLimitKmh;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
