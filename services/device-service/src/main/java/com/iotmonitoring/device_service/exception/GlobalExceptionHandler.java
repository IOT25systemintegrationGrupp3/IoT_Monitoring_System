package com.iotmonitoring.device_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateDeviceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateDevice(
            DuplicateDeviceException exception) {

        return Map.of(
                "error", "DUPLICATE_DEVICE",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleDeviceNotFound(
            DeviceNotFoundException exception) {

        return Map.of(
                "error", "DEVICE_NOT_FOUND",
                "message", exception.getMessage()
        );
    }
}