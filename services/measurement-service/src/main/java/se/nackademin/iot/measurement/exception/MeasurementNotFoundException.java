package se.nackademin.iot.measurement.exception;

import java.util.UUID;

public class MeasurementNotFoundException extends RuntimeException {

    public MeasurementNotFoundException(UUID measurementId) {
        super("Measurement not found: " + measurementId);
    }
}