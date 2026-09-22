package com.iotmonitoring.integration_service.controller;

import com.iotmonitoring.integration_service.dto.MeasurementRequest;
import com.iotmonitoring.integration_service.service.MeasurementForwardingService;
import com.iotmonitoring.integration_service.exception.DeviceNotRegisteredException;
import com.iotmonitoring.integration_service.exception.AlarmServiceUnavailableException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final MeasurementForwardingService forwardingService;

    public IntegrationController(
            MeasurementForwardingService forwardingService) {
        this.forwardingService = forwardingService;
    }

    @PostMapping("/measurements")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Object receiveMeasurement(
            @Valid @RequestBody MeasurementRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false)
            String correlationId) {

        String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;

        return forwardingService.forwardMeasurement(request, effectiveCorrelationId);
    }

    @ExceptionHandler(DeviceNotRegisteredException.class)
    public ResponseEntity<Map<String, String>> handleDeviceNotRegistered(
            DeviceNotRegisteredException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "DEVICE_NOT_FOUND",
                        "message", exception.getMessage(),
                        "deviceId", exception.getDeviceId(),
                        "correlationId", exception.getCorrelationId()
                ));
    }

    @ExceptionHandler(AlarmServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleAlarmServiceFailure(
            AlarmServiceUnavailableException exception) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "ALARM_SERVICE_UNAVAILABLE",
                        "message", exception.getMessage()
                ));
    }
}