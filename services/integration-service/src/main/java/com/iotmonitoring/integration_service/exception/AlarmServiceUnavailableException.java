package com.iotmonitoring.integration_service.exception;

public class AlarmServiceUnavailableException extends RuntimeException {

    public AlarmServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
