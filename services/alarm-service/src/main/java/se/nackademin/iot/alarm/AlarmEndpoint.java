package se.nackademin.iot.alarm;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import se.nackademin.iot.alarm.generated.GetAlarmRequest;
import se.nackademin.iot.alarm.generated.GetAlarmResponse;
import se.nackademin.iot.alarm.generated.GetThresholdRequest;
import se.nackademin.iot.alarm.generated.GetThresholdResponse;
import se.nackademin.iot.alarm.generated.ProcessMeasurementRequest;
import se.nackademin.iot.alarm.generated.ProcessMeasurementResponse;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Endpoint
public class AlarmEndpoint {

    private static final String NAMESPACE_URI =
            "http://iotmonitoring.com/alarm";

    // Threshold för ultraljudssensorn
    private static final double MIN_DISTANCE = 10.0;
    private static final double MAX_DISTANCE = 400.0;

    // Enkel lagring av alarm i minnet
    private final Map<String, AlarmRecord> alarms =
            new ConcurrentHashMap<>();


    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "ProcessMeasurementRequest"
    )
    @ResponsePayload
    public ProcessMeasurementResponse processMeasurement(
            @RequestPayload ProcessMeasurementRequest request) {

        ProcessMeasurementResponse response =
                new ProcessMeasurementResponse();

        // Enkel validering
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

        // Den här SOAP-servicen hanterar distance
        if (!request.getMeasurementType()
                .equalsIgnoreCase("distance")) {

            throw new IllegalArgumentException(
                    "Unsupported measurement type: "
                            + request.getMeasurementType()
            );
        }

        double value = request.getValue();

        boolean alarmTriggered =
                value < MIN_DISTANCE
                || value > MAX_DISTANCE;

        response.setSuccess(true);

        if (alarmTriggered) {

            String alarmId =
                    UUID.randomUUID().toString();

            double triggeredThreshold;

            String message;

            if (value < MIN_DISTANCE) {
                triggeredThreshold = MIN_DISTANCE;
                message =
                        "Distance is below the allowed threshold";
            } else {
                triggeredThreshold = MAX_DISTANCE;
                message =
                        "Distance exceeds the allowed threshold";
            }

            AlarmRecord alarm = new AlarmRecord(
                    alarmId,
                    request.getDeviceId(),
                    request.getMeasurementType(),
                    value,
                    triggeredThreshold,
                    message,
                    request.getTimestamp()
            );

            alarms.put(alarmId, alarm);

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


    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "GetAlarmRequest"
    )
    @ResponsePayload
    public GetAlarmResponse getAlarm(
            @RequestPayload GetAlarmRequest request) {

        if (request.getAlarmId() == null
                || request.getAlarmId().isBlank()) {

            throw new IllegalArgumentException(
                    "Alarm ID is required"
            );
        }

        AlarmRecord alarm =
                alarms.get(request.getAlarmId());

        if (alarm == null) {
            throw new IllegalArgumentException(
                    "Alarm not found: "
                            + request.getAlarmId()
            );
        }

        GetAlarmResponse response =
                new GetAlarmResponse();

        response.setSuccess(true);
        response.setAlarmId(alarm.alarmId());
        response.setDeviceId(alarm.deviceId());
        response.setMeasurementType(
                alarm.measurementType()
        );
        response.setValue(alarm.value());
        response.setThreshold(alarm.threshold());
        response.setMessage(alarm.message());
        response.setTimestamp(alarm.timestamp());

        return response;
    }


    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "GetThresholdRequest"
    )
    @ResponsePayload
    public GetThresholdResponse getThreshold(
            @RequestPayload GetThresholdRequest request) {

        if (request.getMeasurementType() == null
                || request.getMeasurementType().isBlank()) {

            throw new IllegalArgumentException(
                    "Measurement type is required"
            );
        }

        if (!request.getMeasurementType()
                .equalsIgnoreCase("distance")) {

            throw new IllegalArgumentException(
                    "Unsupported measurement type: "
                            + request.getMeasurementType()
            );
        }

        GetThresholdResponse response =
                new GetThresholdResponse();

        response.setSuccess(true);
        response.setMeasurementType("distance");
        response.setUnit("cm");
        response.setMinimumThreshold(MIN_DISTANCE);
        response.setMaximumThreshold(MAX_DISTANCE);

        return response;
    }


    // Intern modell för lagrade alarm
    private record AlarmRecord(
            String alarmId,
            String deviceId,
            String measurementType,
            double value,
            double threshold,
            String message,
            XMLGregorianCalendar timestamp
    ) {
    }
}