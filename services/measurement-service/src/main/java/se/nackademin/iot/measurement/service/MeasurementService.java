package se.nackademin.iot.measurement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.nackademin.iot.measurement.exception.MeasurementNotFoundException;
import se.nackademin.iot.measurement.model.MeasurementRequest;
import se.nackademin.iot.measurement.model.MeasurementResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeasurementService {

    private static final Logger logger =
            LoggerFactory.getLogger(MeasurementService.class);

    private final Map<UUID, MeasurementResponse> measurements =
            new ConcurrentHashMap<>();

    public MeasurementResponse create(MeasurementRequest request) {
        UUID measurementId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        MeasurementResponse measurement = new MeasurementResponse(
                measurementId,
                correlationId,
                request.deviceId(),
                request.measurementType(),
                request.value(),
                request.unit(),
                request.timestamp(),
                "RECEIVED"
        );

        measurements.put(measurementId, measurement);

        logger.info(
                "Measurement received: measurementId={}, correlationId={}, deviceId={}, type={}, value={}, unit={}",
                measurementId,
                correlationId,
                request.deviceId(),
                request.measurementType(),
                request.value(),
                request.unit()
        );

        return measurement;
    }

    public List<MeasurementResponse> findAll() {
        return List.copyOf(measurements.values());
    }

    public MeasurementResponse findById(UUID measurementId) {
        MeasurementResponse measurement = measurements.get(measurementId);

        if (measurement == null) {
            logger.warn(
                    "Measurement not found: measurementId={}",
                    measurementId
            );

            throw new MeasurementNotFoundException(measurementId);
        }

        return measurement;
    }
}