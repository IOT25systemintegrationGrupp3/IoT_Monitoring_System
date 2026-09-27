package se.nackademin.iot.alarm.exception;

public class AlarmServiceException extends RuntimeException {

    public AlarmServiceException(String message) {
        super(message);
    }

    public AlarmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}