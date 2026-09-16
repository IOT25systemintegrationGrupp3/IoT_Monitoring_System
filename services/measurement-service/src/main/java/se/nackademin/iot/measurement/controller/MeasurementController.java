package se.nackademin.iot.measurement.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.nackademin.iot.measurement.model.MeasurementRequest;
import se.nackademin.iot.measurement.model.MeasurementResponse;
import se.nackademin.iot.measurement.service.MeasurementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(
            MeasurementService measurementService) {

        this.measurementService = measurementService;
    }

    @PostMapping
    public ResponseEntity<MeasurementResponse> createMeasurement(
            @Valid @RequestBody MeasurementRequest request) {

        MeasurementResponse measurement =
                measurementService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(measurement);
    }

    @GetMapping
    public ResponseEntity<List<MeasurementResponse>> getAllMeasurements() {

        return ResponseEntity.ok(
                measurementService.findAll()
        );
    }

    @GetMapping("/{measurementId}")
    public ResponseEntity<MeasurementResponse> getMeasurement(
            @PathVariable UUID measurementId) {

        MeasurementResponse measurement =
                measurementService.findById(measurementId);

        return ResponseEntity.ok(measurement);
    }
}