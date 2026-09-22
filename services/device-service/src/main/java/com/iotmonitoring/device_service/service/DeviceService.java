package com.iotmonitoring.device_service.service;

import com.iotmonitoring.device_service.entity.DeviceEntity;
import com.iotmonitoring.device_service.exception.DuplicateDeviceException;
import com.iotmonitoring.device_service.exception.DeviceNotFoundException;
import com.iotmonitoring.device_service.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceEntity registerDevice(DeviceEntity device) {

        if (deviceRepository.existsByDeviceId(device.getDeviceId())) {
            throw new DuplicateDeviceException(
                    "Device ID already exists: " + device.getDeviceId()
            );
        }

        return deviceRepository.save(device);
    }

    public List<DeviceEntity> getAllDevices() {
        return deviceRepository.findAll();
    }

    public DeviceEntity getDeviceByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(
                        "Device not found: " + deviceId
                ));
    }
}