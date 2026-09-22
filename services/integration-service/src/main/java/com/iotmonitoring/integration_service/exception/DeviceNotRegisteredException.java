package com.iotmonitoring.integration_service.exception;

public class DeviceNotRegisteredException extends RuntimeException {

    private final String deviceId;
    private final String correlationId;

    public DeviceNotRegisteredException(String deviceId, String correlationId) {
        super("Device is not registered: " + deviceId);
        this.deviceId = deviceId;
        this.correlationId = correlationId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
