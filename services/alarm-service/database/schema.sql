CREATE TABLE thresholds (
    measurement_type VARCHAR(100) PRIMARY KEY,
    unit VARCHAR(50) NOT NULL,
    minimum_threshold FLOAT NOT NULL,
    maximum_threshold FLOAT NOT NULL
);

CREATE TABLE alarms (
    id VARCHAR(100) PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    measurement_type VARCHAR(100) NOT NULL,
    value FLOAT NOT NULL,
    threshold FLOAT NOT NULL,
    message VARCHAR(255),
    timestamp DATETIMEOFFSET NOT NULL
);

INSERT INTO thresholds (
    measurement_type,
    unit,
    minimum_threshold,
    maximum_threshold
)
VALUES (
    'distance',
    'cm',
    10.0,
    400.0
);