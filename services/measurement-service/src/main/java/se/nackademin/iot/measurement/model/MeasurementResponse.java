package se.nackademin.iot.measurement.model;

import java.time.Instant;
import java.util.UUID;

public record MeasurementResponse(

        UUID measurementId,
        UUID correlationId,
        String deviceId,
        String measurementType,
        Double value,
        String unit,
        Instant timestamp,
        String status

) {
}