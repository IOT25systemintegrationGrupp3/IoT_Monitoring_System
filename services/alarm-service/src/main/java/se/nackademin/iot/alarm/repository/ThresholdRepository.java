package se.nackademin.iot.alarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.nackademin.iot.alarm.model.Threshold;

public interface ThresholdRepository
        extends JpaRepository<Threshold, String> {
}