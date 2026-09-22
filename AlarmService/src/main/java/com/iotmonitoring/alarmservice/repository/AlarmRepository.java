package com.iotmonitoring.alarmservice.repository;

import com.iotmonitoring.alarmservice.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
    List<Alarm> findAllByOrderByCreatedAtDesc();
}