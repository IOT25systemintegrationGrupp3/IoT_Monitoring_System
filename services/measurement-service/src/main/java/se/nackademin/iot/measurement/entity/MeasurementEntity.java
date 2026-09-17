package se.nackademin.iot.measurement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import se.nackademin.iot.measurement.model.MeasurementResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Measurements", schema = "dbo")
public class MeasurementEntity {

    @Id
    @Column(name = "measurement_id", nullable = false)
    private UUID measurementId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "measurement_type", nullable = false)
    private String measurementType;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(name = "status", nullable = false)
    private String status;

    protected MeasurementEntity() {
    }

    public MeasurementEntity(
            UUID measurementId,
            UUID correlationId,
            String deviceId,
            String measurementType,
            Double value,
            String unit,
            OffsetDateTime measuredAt,
            String status
    ) {
        this.measurementId = measurementId;
        this.correlationId = correlationId;
        this.deviceId = deviceId;
        this.measurementType = measurementType;
        this.value = value;
        this.unit = unit;
        this.measuredAt = measuredAt;
        this.status = status;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public MeasurementResponse toResponse() {
        return new MeasurementResponse(
                measurementId,
                correlationId,
                deviceId,
                measurementType,
                value,
                unit,
                measuredAt.toInstant(),
                status
        );
    }
}