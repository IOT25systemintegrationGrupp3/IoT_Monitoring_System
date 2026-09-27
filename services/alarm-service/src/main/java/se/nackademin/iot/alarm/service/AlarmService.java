package se.nackademin.iot.alarm.service;

import org.springframework.stereotype.Service;

import se.nackademin.iot.alarm.generated.GetAlarmRequest;
import se.nackademin.iot.alarm.generated.GetAlarmResponse;
import se.nackademin.iot.alarm.generated.GetThresholdRequest;
import se.nackademin.iot.alarm.generated.GetThresholdResponse;
import se.nackademin.iot.alarm.generated.ProcessMeasurementRequest;
import se.nackademin.iot.alarm.generated.ProcessMeasurementResponse;
import se.nackademin.iot.alarm.model.Alarm;
import se.nackademin.iot.alarm.model.Threshold;
import se.nackademin.iot.alarm.repository.AlarmRepository;
import se.nackademin.iot.alarm.repository.ThresholdRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final ThresholdRepository thresholdRepository;

    public AlarmService(
            AlarmRepository alarmRepository,
            ThresholdRepository thresholdRepository) {

        this.alarmRepository = alarmRepository;
        this.thresholdRepository = thresholdRepository;
    }

    public ProcessMeasurementResponse processMeasurement(
            ProcessMeasurementRequest request) {

        ProcessMeasurementResponse response =
                new ProcessMeasurementResponse();

        if (request.getDeviceId() == null
                || request.getDeviceId().isBlank()) {

            throw new IllegalArgumentException(
                    "Device ID is required"
            );
        }

        if (request.getMeasurementType() == null
                || request.getMeasurementType().isBlank()) {

            throw new IllegalArgumentException(
                    "Measurement type is required"
            );
        }

        Threshold threshold =
                thresholdRepository
                        .findById(request.getMeasurementType())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unsupported measurement type: "
                                                + request.getMeasurementType()
                                )
                        );

        double value = request.getValue();

        boolean alarmTriggered =
                value < threshold.getMinimumThreshold()
                        || value > threshold.getMaximumThreshold();

        response.setSuccess(true);

        if (alarmTriggered) {

            String alarmId =
                    UUID.randomUUID().toString();

            double triggeredThreshold;
            String message;

            if (value < threshold.getMinimumThreshold()) {

                triggeredThreshold =
                        threshold.getMinimumThreshold();

                message =
                        "Value is below the allowed threshold";

            } else {

                triggeredThreshold =
                        threshold.getMaximumThreshold();

                message =
                        "Value exceeds the allowed threshold";
            }

            OffsetDateTime timestamp =
                    request.getTimestamp()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toOffsetDateTime();

            Alarm alarm = new Alarm(
                    alarmId,
                    request.getDeviceId(),
                    request.getMeasurementType(),
                    value,
                    triggeredThreshold,
                    message,
                    timestamp
            );

            alarmRepository.save(alarm);

            response.setAlarmCreated(true);
            response.setAlarmId(alarmId);
            response.setMessage(message);

        } else {

            response.setAlarmCreated(false);
            response.setAlarmId("");
            response.setMessage(
                    "Measurement is within the allowed threshold"
            );
        }

        return response;
    }

    public GetAlarmResponse getAlarm(
            GetAlarmRequest request) {

        if (request.getAlarmId() == null
                || request.getAlarmId().isBlank()) {

            throw new IllegalArgumentException(
                    "Alarm ID is required"
            );
        }

        Alarm alarm =
                alarmRepository
                        .findById(request.getAlarmId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Alarm not found: "
                                                + request.getAlarmId()
                                )
                        );

        GetAlarmResponse response =
                new GetAlarmResponse();

        response.setSuccess(true);
        response.setAlarmId(alarm.getId());
        response.setDeviceId(alarm.getDeviceId());
        response.setMeasurementType(
                alarm.getMeasurementType()
        );
        response.setValue(alarm.getValue());
        response.setThreshold(alarm.getThreshold());
        response.setMessage(alarm.getMessage());

        return response;
    }

    public GetThresholdResponse getThreshold(
            GetThresholdRequest request) {

        if (request.getMeasurementType() == null
                || request.getMeasurementType().isBlank()) {

            throw new IllegalArgumentException(
                    "Measurement type is required"
            );
        }

        Threshold threshold =
                thresholdRepository
                        .findById(request.getMeasurementType())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unsupported measurement type: "
                                                + request.getMeasurementType()
                                )
                        );

        GetThresholdResponse response =
                new GetThresholdResponse();

        response.setSuccess(true);
        response.setMeasurementType(
                threshold.getMeasurementType()
        );
        response.setUnit(
                threshold.getUnit()
        );
        response.setMinimumThreshold(
                threshold.getMinimumThreshold()
        );
        response.setMaximumThreshold(
                threshold.getMaximumThreshold()
        );

        return response;
    }
}