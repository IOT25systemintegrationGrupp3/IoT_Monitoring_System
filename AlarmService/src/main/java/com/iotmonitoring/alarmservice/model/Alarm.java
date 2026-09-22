package com.iotmonitoring.alarmservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alarms", schema = "dbo")
public class Alarm {

    public enum Severity { INFO, WARNING, CRITICAL }

    public enum Status { ACTIVE, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 50)
    private String measurementType;

    @Column(nullable = false, precision = 18, scale = 6)
    private java.math.BigDecimal value;

    @Column(nullable = false, length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "measurement_timestamp", nullable = false, columnDefinition = "datetimeoffset")
    private OffsetDateTime timestamp;

    @Column(nullable = false, columnDefinition = "datetimeoffset")
    private OffsetDateTime createdAt;

    protected Alarm() {
    }

    public Alarm(String deviceId, String measurementType, double value,
                 String unit, Severity severity, Status status,
                 String message, OffsetDateTime timestamp, OffsetDateTime createdAt) {
        this.deviceId = deviceId;
        this.measurementType = measurementType;
        this.value = java.math.BigDecimal.valueOf(value);
        this.unit = unit;
        this.severity = severity;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
    }

    public Long getAlarmId() { return alarmId; }
    public String getDeviceId() { return deviceId; }
    public String getMeasurementType() { return measurementType; }
    public java.math.BigDecimal getValue() { return value; }
    public String getUnit() { return unit; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}