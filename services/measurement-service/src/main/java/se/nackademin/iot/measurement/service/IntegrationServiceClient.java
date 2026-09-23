package se.nackademin.iot.measurement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.nackademin.iot.measurement.model.MeasurementRequest;

import java.util.UUID;

@Component
public class IntegrationServiceClient {

    private static final Logger logger =
            LoggerFactory.getLogger(IntegrationServiceClient.class);

    private final RestClient restClient;

    public IntegrationServiceClient(
            @Value("${INTEGRATION_SERVICE_URL:http://localhost:8081}")
            String integrationServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(integrationServiceUrl)
                .build();
    }

    public void sendMeasurement(
        MeasurementRequest request,
        UUID correlationId) {

    logger.info(
            "Sending measurement to Integration Service: correlationId={}, deviceId={}, type={}, value={}",
            correlationId,
            request.deviceId(),
            request.measurementType(),
            request.value()
    );

    try {

        restClient.post()
                .uri("/api/integration/measurements")
                .header("X-Correlation-ID", correlationId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        logger.info(
                "Measurement successfully sent to Integration Service: correlationId={}",
                correlationId
        );

    } catch (Exception e) {

        logger.error(
                "Failed to send measurement to Integration Service: correlationId={}, error={}",
                correlationId,
                e.getMessage(),
                e
        );

        throw e;
    }
}
