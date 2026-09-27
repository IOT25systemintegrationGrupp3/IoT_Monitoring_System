package se.nackademin.iot.alarm.endpoint;

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
import se.nackademin.iot.alarm.service.AlarmService;

@Endpoint
public class AlarmEndpoint {

    private static final String NAMESPACE_URI =
            "http://iotmonitoring.com/alarm";

    private final AlarmService alarmService;

    public AlarmEndpoint(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "ProcessMeasurementRequest"
    )
    @ResponsePayload
    public ProcessMeasurementResponse processMeasurement(
            @RequestPayload ProcessMeasurementRequest request) {

        return alarmService.processMeasurement(request);
    }

    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "GetAlarmRequest"
    )
    @ResponsePayload
    public GetAlarmResponse getAlarm(
            @RequestPayload GetAlarmRequest request) {

        return alarmService.getAlarm(request);
    }

    @PayloadRoot(
            namespace = NAMESPACE_URI,
            localPart = "GetThresholdRequest"
    )
    @ResponsePayload
    public GetThresholdResponse getThreshold(
            @RequestPayload GetThresholdRequest request) {

        return alarmService.getThreshold(request);
    }
}