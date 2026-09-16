package se.nackademin.iot.measurement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MeasurementRequest(

        @NotBlank(message = "deviceId is required")
        String deviceId,

        @NotBlank(message = "measurementType is required")
        String measurementType,

        @NotNull(message = "value is required")
        Double value,

        @NotBlank(message = "unit is required")
        String unit,

        @NotNull(message = "timestamp is required")
        Instant timestamp

) {
}