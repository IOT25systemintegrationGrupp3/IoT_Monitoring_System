package se.nackademin.iot.alarm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "thresholds")
public class Threshold {

    @Id
    @Column(name = "measurement_type")
    private String measurementType;

    @Column(name = "unit")
    private String unit;

    @Column(name = "minimum_threshold")
    private double minimumThreshold;

    @Column(name = "maximum_threshold")
    private double maximumThreshold;

    public Threshold() {
    }

    public Threshold(
            String measurementType,
            String unit,
            double minimumThreshold,
            double maximumThreshold) {

        this.measurementType = measurementType;
        this.unit = unit;
        this.minimumThreshold = minimumThreshold;
        this.maximumThreshold = maximumThreshold;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public String getUnit() {
        return unit;
    }

    public double getMinimumThreshold() {
        return minimumThreshold;
    }

    public double getMaximumThreshold() {
        return maximumThreshold;
    }
}