package com.iotmonitoring.device_service.exception;

public class DuplicateDeviceException extends RuntimeException {

    public DuplicateDeviceException(String message) {
        super(message);
    }
}