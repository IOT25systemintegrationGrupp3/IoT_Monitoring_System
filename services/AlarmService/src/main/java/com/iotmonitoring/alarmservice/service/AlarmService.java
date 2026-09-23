package com.iotmonitoring.alarmservice.service;

import com.iotmonitoring.alarmservice.exception.AlarmNotFoundException;
import com.iotmonitoring.alarmservice.exception.AlarmValidationException;
import com.iotmonitoring.alarmservice.model.Alarm;
import com.iotmonitoring.alarmservice.repository.AlarmRepository;
import com.iotmonitoring.alarmservice.ws.GetAlarmResponseType;
import com.iotmonitoring.alarmservice.ws.GetThresholdResponseType;
import com.iotmonitoring.alarmservice.ws.ProcessMeasurementResponseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Map;

@Service
public class AlarmService {

    private static final ZoneId STOCKHOLM = ZoneId.of("Europe/Stockholm");

    private static final Map<String, ThresholdRule> RULES = Map.of(
            "water_leak", new ThresholdRule("boolean", 1.0, 0.0, 1.0),
            "temperature", new ThresholdRule("C", 30.0, Double.NEGATIVE_INFINITY, 30.0),
            "humidity", new ThresholdRule("%", 80.0, Double.NEGATIVE_INFINITY, 80.0),
            "distance", new ThresholdRule("cm", Double.POSITIVE_INFINITY, 10.0, 10.0)
    );

    private final AlarmRepository repository;
    private final AlarmEmailService alarmEmailService;

    public AlarmService(AlarmRepository repository, AlarmEmailService alarmEmailService) {
        this.repository = repository;
        this.alarmEmailService = alarmEmailService;
    }

    @Transactional
    public ProcessMeasurementResponseType processMeasurement(
            String deviceId, String measurementType, double value,
            String unit, OffsetDateTime timestamp) {

        validateMeasurement(deviceId, measurementType, unit, timestamp);
        ThresholdRule rule = ruleFor(measurementType);

        ProcessMeasurementResponseType response = new ProcessMeasurementResponseType();
        response.setSuccess(true);
        response.setAlarmCreated(false);
        response.setMessage("Measurement is within the allowed threshold");

        boolean alarmTriggered;

        if ("water_leak".equalsIgnoreCase(measurementType)) {
            alarmTriggered = value == 1.0d;
        } else if ("distance".equalsIgnoreCase(measurementType)) {
            alarmTriggered = value <= rule.minimumThreshold();
        } else {
            alarmTriggered = value >= rule.maximumThreshold();
        }

        if (alarmTriggered) {
            String message = "water_leak".equalsIgnoreCase(measurementType)
                    ? "Water leak detected"
                    : "distance".equalsIgnoreCase(measurementType)
                    ? "Distance is below the allowed threshold"
                    : measurementType + " exceeds the allowed threshold";

            Alarm alarm = repository.save(new Alarm(
                    deviceId, measurementType, value, unit,
                    Alarm.Severity.CRITICAL, Alarm.Status.ACTIVE,
                    message, timestamp, nowInStockholm()));

            alarmEmailService.sendAlarmNotification(alarm);
            response.setAlarmCreated(true);
            response.setAlarmId(String.valueOf(alarm.getAlarmId()));
            response.setMessage(alarm.getMessage());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Alarm getAlarm(String alarmId) {
        long numericId;
        try {
            numericId = Long.parseLong(alarmId);
        } catch (NumberFormatException exception) {
            throw new AlarmValidationException("INVALID_ALARM_ID", "alarmId must be numeric");
        }
        return repository.findById(numericId)
                .orElseThrow(() -> new AlarmNotFoundException(alarmId));
    }

    public GetAlarmResponseType toGetAlarmResponse(Alarm alarm) {
        ThresholdRule rule = ruleFor(alarm.getMeasurementType());
        GetAlarmResponseType response = new GetAlarmResponseType();
        response.setSuccess(true);
        response.setAlarmId(String.valueOf(alarm.getAlarmId()));
        response.setDeviceId(alarm.getDeviceId());
        response.setMeasurementType(alarm.getMeasurementType());
        response.setValue(alarm.getValue().doubleValue());
        response.setThreshold(rule.maximumThreshold());
        response.setMessage(alarm.getMessage());
        response.setTimestamp(toXml(alarm.getTimestamp()));
        return response;
    }

    public GetThresholdResponseType getThreshold(String measurementType) {
        ThresholdRule rule = ruleFor(measurementType);
        GetThresholdResponseType response = new GetThresholdResponseType();
        response.setSuccess(true);
        response.setMeasurementType(measurementType);
        response.setUnit(rule.unit());
        response.setMaximumThreshold(rule.maximumThreshold());
        response.setMinimumThreshold(rule.minimumThreshold());
        return response;
    }

    private ThresholdRule ruleFor(String measurementType) {
        if (measurementType == null || measurementType.isBlank()) {
            throw new AlarmValidationException("MISSING_MEASUREMENT_TYPE", "measurementType is required");
        }
        ThresholdRule rule = RULES.get(measurementType.toLowerCase());
        if (rule == null) {
            throw new AlarmValidationException("UNSUPPORTED_MEASUREMENT_TYPE",
                    "Unsupported measurement type: " + measurementType);
        }
        return rule;
    }

    private void validateMeasurement(String deviceId, String measurementType,
                                     String unit, OffsetDateTime timestamp) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new AlarmValidationException("MISSING_DEVICE_ID", "deviceId is required");
        }
        if (unit == null || unit.isBlank()) {
            throw new AlarmValidationException("MISSING_UNIT", "unit is required");
        }
        if (timestamp == null) {
            throw new AlarmValidationException("MISSING_TIMESTAMP", "timestamp is required");
        }
    }

    private OffsetDateTime nowInStockholm() {
        return ZonedDateTime.now(STOCKHOLM).toOffsetDateTime();
    }

    private XMLGregorianCalendar toXml(OffsetDateTime timestamp) {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(timestamp.atZoneSameInstant(STOCKHOLM));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to convert alarm timestamp", exception);
        }
    }

    private record ThresholdRule(String unit, double maximumThreshold, double minimumThreshold, double alarmValue) {
    }
}
