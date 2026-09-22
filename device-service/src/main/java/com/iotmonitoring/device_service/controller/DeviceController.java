package com.iotmonitoring.device_service.controller;

import com.iotmonitoring.device_service.dto.DeviceRequest;
import com.iotmonitoring.device_service.entity.DeviceEntity;
import com.iotmonitoring.device_service.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceEntity registerDevice(
            @Valid @RequestBody DeviceRequest request) {

        DeviceEntity device = new DeviceEntity();

        device.setDeviceId(request.getDeviceId());
        device.setName(request.getName());
        device.setDeviceType(request.getDeviceType());
        device.setLocation(request.getLocation());

        return deviceService.registerDevice(device);
    }

    @GetMapping
    public List<DeviceEntity> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{deviceId}")
    public DeviceEntity getDeviceById(
            @PathVariable String deviceId) {

        return deviceService.getDeviceByDeviceId(deviceId);
    }
}