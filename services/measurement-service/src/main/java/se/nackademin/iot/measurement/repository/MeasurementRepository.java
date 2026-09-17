package se.nackademin.iot.measurement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.nackademin.iot.measurement.entity.MeasurementEntity;

import java.util.List;
import java.util.UUID;

public interface MeasurementRepository
        extends JpaRepository<MeasurementEntity, UUID> {

    List<MeasurementEntity> findAllByOrderByMeasuredAtDesc();
}