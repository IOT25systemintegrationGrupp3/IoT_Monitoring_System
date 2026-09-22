package com.iotmonitoring.integration_service.controller;

import com.iotmonitoring.integration_service.alarm.ws.ProcessMeasurementResponseType;
import com.iotmonitoring.integration_service.dto.MeasurementRequest;
import com.iotmonitoring.integration_service.exception.AlarmServiceUnavailableException;
import com.iotmonitoring.integration_service.service.MeasurementForwardingService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationControllerTest {

    @Test
    void shouldForwardMeasurementWithExistingCorrelationId() {
        MeasurementForwardingService forwardingService = mock(MeasurementForwardingService.class);
        IntegrationController controller = new IntegrationController(forwardingService);
        MeasurementRequest request = new MeasurementRequest();
        request.setDeviceId("water-leak-esp32-01");
        request.setMeasurementType("water_leak");
        request.setValue(1.0);
        request.setUnit("boolean");
        request.setTimestamp(OffsetDateTime.parse("2026-09-22T12:00:00+02:00"));
        ProcessMeasurementResponseType response = new ProcessMeasurementResponseType();
        response.setSuccess(true);
        response.setAlarmCreated(true);
        response.setAlarmId("3");
        response.setMessage("Water leak detected");

        when(forwardingService.forwardMeasurement(request, "correlation-123"))
                .thenReturn(response);

        Object result = controller.receiveMeasurement(request, "correlation-123");

        assertSame(response, result);
        verify(forwardingService).forwardMeasurement(eq(request), eq("correlation-123"));
    }

    @Test
    void shouldReturn503WhenAlarmServiceIsUnavailable() {
        MeasurementForwardingService forwardingService = mock(MeasurementForwardingService.class);
        IntegrationController controller = new IntegrationController(forwardingService);
        MeasurementRequest request = new MeasurementRequest();
        request.setDeviceId("water-leak-esp32-01");

        AlarmServiceUnavailableException failure =
                new AlarmServiceUnavailableException("Alarm Service is unavailable", new RuntimeException());
        when(forwardingService.forwardMeasurement(request, "correlation-123"))
                .thenThrow(failure);

        var response = controller.handleAlarmServiceFailure(failure);

        assertEquals(503, response.getStatusCode().value());
        assertEquals("ALARM_SERVICE_UNAVAILABLE", response.getBody().get("error"));
    }
}
