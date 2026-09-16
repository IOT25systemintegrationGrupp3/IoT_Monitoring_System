package se.nackademin.iot.measurement.exception;

import java.time.Instant;
import java.util.UUID;

public record ErrorResponse(

        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        UUID correlationId

) {
}