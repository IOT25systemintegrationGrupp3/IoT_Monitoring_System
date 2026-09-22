package com.iotmonitoring.alarmservice.endpoint;

import com.iotmonitoring.alarmservice.model.Alarm;
import com.iotmonitoring.alarmservice.service.AlarmService;
import com.iotmonitoring.alarmservice.ws.GetAlarmRequestType;
import com.iotmonitoring.alarmservice.ws.GetAlarmResponseType;
import com.iotmonitoring.alarmservice.ws.GetThresholdRequestType;
import com.iotmonitoring.alarmservice.ws.GetThresholdResponseType;
import com.iotmonitoring.alarmservice.ws.ObjectFactory;
import com.iotmonitoring.alarmservice.ws.ProcessMeasurementRequestType;
import com.iotmonitoring.alarmservice.ws.ProcessMeasurementResponseType;
import jakarta.xml.bind.JAXBElement;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Endpoint
public class AlarmEndpoint {

    private static final String NAMESPACE = "http://iotmonitoring.com/alarm";
    private static final ZoneId STOCKHOLM = ZoneId.of("Europe/Stockholm");
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final AlarmService service;

    public AlarmEndpoint(AlarmService service) {
        this.service = service;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "ProcessMeasurementRequest")
    @ResponsePayload
    public JAXBElement<ProcessMeasurementResponseType> processMeasurement(
            @RequestPayload JAXBElement<ProcessMeasurementRequestType> request) {

        ProcessMeasurementRequestType payload = request.getValue();
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(
                payload.getTimestamp().toGregorianCalendar().toInstant(), STOCKHOLM);
        return OBJECT_FACTORY.createProcessMeasurementResponse(
                service.processMeasurement(
                        payload.getDeviceId(), payload.getMeasurementType(),
                        payload.getValue(), payload.getUnit(), timestamp));
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetAlarmRequest")
    @ResponsePayload
    public JAXBElement<GetAlarmResponseType> getAlarm(
            @RequestPayload JAXBElement<GetAlarmRequestType> request) {

        GetAlarmRequestType payload = request.getValue();
        Alarm alarm = service.getAlarm(payload.getAlarmId());
        return OBJECT_FACTORY.createGetAlarmResponse(service.toGetAlarmResponse(alarm));
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetThresholdRequest")
    @ResponsePayload
    public JAXBElement<GetThresholdResponseType> getThreshold(
            @RequestPayload JAXBElement<GetThresholdRequestType> request) {

        return OBJECT_FACTORY.createGetThresholdResponse(
                service.getThreshold(request.getValue().getMeasurementType()));
    }
}
