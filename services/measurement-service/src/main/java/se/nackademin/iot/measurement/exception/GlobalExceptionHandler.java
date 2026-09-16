package se.nackademin.iot.measurement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        UUID correlationId = UUID.randomUUID();

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " +
                        error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        logger.warn(
                "Validation failed. correlationId={}, path={}, message={}",
                correlationId,
                request.getRequestURI(),
                message
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                message,
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        UUID correlationId = UUID.randomUUID();

        String message =
                "JSON is malformed or contains an invalid field type";

        logger.warn(
                "Invalid JSON. correlationId={}, path={}",
                correlationId,
                request.getRequestURI()
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid JSON",
                message,
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MeasurementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMeasurementNotFound(
            MeasurementNotFoundException exception,
            HttpServletRequest request) {

        UUID correlationId = UUID.randomUUID();

        logger.warn(
                "Measurement not found. correlationId={}, path={}, message={}",
                correlationId,
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Measurement not found",
                exception.getMessage(),
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}