package se.nackademin.iot.alarm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alarms")
public class Alarm {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "measurement_type")
    private String measurementType;

    @Column(name = "value")
    private double value;

    @Column(name = "threshold")
    private double threshold;

    @Column(name = "message")
    private String message;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;

    public Alarm() {
    }

    public Alarm(
            String id,
            String deviceId,
            String measurementType,
            double value,
            double threshold,
            String message,
            OffsetDateTime timestamp) {

        this.id = id;
        this.deviceId = deviceId;
        this.measurementType = measurementType;
        this.value = value;
        this.threshold = threshold;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public double getValue() {
        return value;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}