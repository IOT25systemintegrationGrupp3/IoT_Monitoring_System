package com.iotmonitoring.integration_service.service;

import com.iotmonitoring.integration_service.alarm.ws.ObjectFactory;
import com.iotmonitoring.integration_service.alarm.ws.ProcessMeasurementRequestType;
import com.iotmonitoring.integration_service.alarm.ws.ProcessMeasurementResponseType;
import com.iotmonitoring.integration_service.dto.MeasurementRequest;
import com.iotmonitoring.integration_service.exception.AlarmServiceUnavailableException;
import jakarta.xml.bind.JAXBElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import javax.xml.datatype.DatatypeFactory;
import java.time.OffsetDateTime;

@Service
public class AlarmSoapClient {

    private static final Logger logger = LoggerFactory.getLogger(AlarmSoapClient.class);
    private static final String SOAP_NAMESPACE = "http://iotmonitoring.com/alarm";

    private final WebServiceTemplate webServiceTemplate;
    private final String alarmServiceUrl;
    private final ObjectFactory objectFactory = new ObjectFactory();

    public AlarmSoapClient(
            WebServiceTemplate webServiceTemplate,
            @Value("${ALARM_SERVICE_URL:http://localhost:8083/ws}") String alarmServiceUrl) {
        this.webServiceTemplate = webServiceTemplate;
        this.alarmServiceUrl = alarmServiceUrl;
    }

    public ProcessMeasurementResponseType processMeasurement(
            MeasurementRequest measurement,
            String correlationId) {
        try {
            ProcessMeasurementRequestType payload = new ProcessMeasurementRequestType();
            payload.setDeviceId(measurement.getDeviceId());
            payload.setMeasurementType(measurement.getMeasurementType());
            payload.setValue(measurement.getValue());
            payload.setUnit(measurement.getUnit());
            payload.setTimestamp(toXml(measurement.getTimestamp()));

            JAXBElement<ProcessMeasurementRequestType> request =
                    objectFactory.createProcessMeasurementRequest(payload);

            JAXBElement<?> rawResponse = (JAXBElement<?>) webServiceTemplate.marshalSendAndReceive(
                    alarmServiceUrl,
                    request
            );
            ProcessMeasurementResponseType response =
                    (ProcessMeasurementResponseType) rawResponse.getValue();

            if (response.isAlarmCreated()) {
                logger.warn(
                        "Alarm Service created alarm: correlationId={}, alarmId={}, message={}",
                        correlationId, response.getAlarmId(), response.getMessage()
                );
            } else {
                logger.info("Alarm Service created no alarm: correlationId={}, message={}",
                        correlationId, response.getMessage());
            }
            return response;
        } catch (SoapFaultClientException exception) {
            logger.error("Alarm Service returned a SOAP Fault: correlationId={}, fault={}",
                    correlationId, exception.getFaultStringOrReason());
            throw new AlarmServiceUnavailableException(
                "Alarm Service rejected the measurement request", exception);
        } catch (RuntimeException exception) {
            logger.error(
                "Alarm Service call failed; correlationId={}",
                    correlationId,
                    exception
            );
            throw new AlarmServiceUnavailableException(
                "Alarm Service is unavailable", exception);
        }
    }

    private javax.xml.datatype.XMLGregorianCalendar toXml(OffsetDateTime timestamp) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    java.util.GregorianCalendar.from(timestamp.toZonedDateTime())
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid measurement timestamp", exception);
        }
    }
}
