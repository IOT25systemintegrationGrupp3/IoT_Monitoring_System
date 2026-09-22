package com.iotmonitoring.integration_service.service;

import com.iotmonitoring.integration_service.dto.MeasurementRequest;
import com.iotmonitoring.integration_service.alarm.ws.ProcessMeasurementResponseType;
import com.iotmonitoring.integration_service.exception.DeviceNotRegisteredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MeasurementForwardingService {

        private static final Logger logger =
                        LoggerFactory.getLogger(MeasurementForwardingService.class);

    private final RestClient deviceRestClient;
    private final AlarmSoapClient alarmSoapClient;

    public MeasurementForwardingService(
            RestClient.Builder builder,
            @Value("${DEVICE_SERVICE_URL:}") String deviceServiceUrl,
            AlarmSoapClient alarmSoapClient) {
        this.alarmSoapClient = alarmSoapClient;

        if (deviceServiceUrl.isBlank()) {
            throw new IllegalStateException(
                    "DEVICE_SERVICE_URL environment variable is required"
            );
        }

        this.deviceRestClient = builder
                .baseUrl(deviceServiceUrl)
                .build();
    }

    public ProcessMeasurementResponseType forwardMeasurement(
            MeasurementRequest request,
            String correlationId) {

        // Step 1: Check whether the device is registered
        deviceRestClient.get()
                .uri("/api/devices/{deviceId}", request.getDeviceId())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (httpRequest, response) -> {
                                                        if (response.getStatusCode().value() == 404) {
                                                                logger.warn(
                                                                                "Device Service rejected measurement: deviceId={}, correlationId={}, status=404",
                                                                                request.getDeviceId(),
                                                                                correlationId
                                                                );
                                                                throw new DeviceNotRegisteredException(
                                                                                request.getDeviceId(),
                                                                                correlationId
                                                                );
                                                        }
                            throw new IllegalArgumentException(
                                    "Device is not registered: "
                                            + request.getDeviceId()
                            );
                        }
                )
                .toBodilessEntity();

        // Step 2: Ask Alarm Service to process the already-validated measurement
        return alarmSoapClient.processMeasurement(request, correlationId);
    }
}