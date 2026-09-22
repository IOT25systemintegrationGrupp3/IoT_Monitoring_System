package com.iotmonitoring.alarmservice.exception;

public class AlarmNotFoundException extends RuntimeException {
    public AlarmNotFoundException(String alarmId) {
        super("Alarm with ID " + alarmId + " does not exist");
    }
}
