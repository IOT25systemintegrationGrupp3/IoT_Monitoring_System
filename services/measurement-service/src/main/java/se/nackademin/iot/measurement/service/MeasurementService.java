package se.nackademin.iot.measurement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.nackademin.iot.measurement.entity.MeasurementEntity;
import se.nackademin.iot.measurement.exception.MeasurementNotFoundException;
import se.nackademin.iot.measurement.model.MeasurementRequest;
import se.nackademin.iot.measurement.model.MeasurementResponse;
import se.nackademin.iot.measurement.repository.MeasurementRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class MeasurementService {

    private static final Logger logger =
            LoggerFactory.getLogger(MeasurementService.class);

   private final MeasurementRepository measurementRepository;
private final IntegrationServiceClient integrationServiceClient;

public MeasurementService(
        MeasurementRepository measurementRepository,
        IntegrationServiceClient integrationServiceClient) {

    this.measurementRepository = measurementRepository;
    this.integrationServiceClient = integrationServiceClient;
}

    @Transactional
    public MeasurementResponse create(MeasurementRequest request) {
        UUID measurementId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        MeasurementEntity entity = new MeasurementEntity(
                measurementId,
                correlationId,
                request.deviceId(),
                request.measurementType(),
                request.value(),
                request.unit(),
                OffsetDateTime.ofInstant(
                        request.timestamp(),
                        ZoneOffset.UTC
                ),
                "RECEIVED"
        );

        MeasurementEntity savedMeasurement =
                measurementRepository.save(entity);

        logger.info(
                "Measurement saved in SQL: measurementId={}, correlationId={}, deviceId={}, type={}, value={}, unit={}",
                measurementId,
                correlationId,
                request.deviceId(),
                request.measurementType(),
                request.value(),
                request.unit()
        );

        integrationServiceClient.sendMeasurement(request, correlationId);

        return savedMeasurement.toResponse();
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> findAll() {
        return measurementRepository
                .findAllByOrderByMeasuredAtDesc()
                .stream()
                .map(MeasurementEntity::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeasurementResponse findById(UUID measurementId) {
        return measurementRepository
                .findById(measurementId)
                .map(MeasurementEntity::toResponse)
                .orElseThrow(() -> {
                    logger.warn(
                            "Measurement not found: measurementId={}",
                            measurementId
                    );

                    return new MeasurementNotFoundException(measurementId);
                });
    }
}
