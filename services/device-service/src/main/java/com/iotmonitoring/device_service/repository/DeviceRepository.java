package com.iotmonitoring.device_service.repository;

import com.iotmonitoring.device_service.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {

    Optional<DeviceEntity> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}