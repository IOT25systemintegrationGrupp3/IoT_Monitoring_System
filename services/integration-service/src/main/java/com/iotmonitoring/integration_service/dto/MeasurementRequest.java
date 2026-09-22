
package com.iotmonitoring.integration_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class MeasurementRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Measurement type is required")
    private String measurementType;

    @NotNull(message = "Value is required")
    private Double value;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Timestamp is required")
    private OffsetDateTime timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}