package se.nackademin.iot.alarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.nackademin.iot.alarm.model.Alarm;

public interface AlarmRepository extends JpaRepository<Alarm, String> {
}