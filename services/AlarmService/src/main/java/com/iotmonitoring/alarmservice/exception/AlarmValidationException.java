package com.iotmonitoring.alarmservice.exception;

public class AlarmValidationException extends RuntimeException {
    private final String errorCode;

    public AlarmValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
