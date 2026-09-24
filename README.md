# IoT Monitoring System

An event-driven IoT monitoring platform built from Spring Boot services. The system registers IoT devices, accepts measurements, stores them with correlation IDs, validates that the source device is known, and evaluates measurements against alarm thresholds. When a measurement crosses a threshold, the Alarm Service persists an alarm and can send an email notification.

The project demonstrates a small distributed system with REST APIs for public-facing workflows and SOAP for the shared alarm-processing contract.

## Project At A Glance

- **Device Service**: registers devices and exposes the device registry.
- **Measurement Service**: accepts and stores incoming measurements.
- **Integration Service**: coordinates device validation and forwards measurements to the Alarm Service.
- **Alarm Service**: evaluates measurements, stores alarms, exposes the SOAP contract, and sends optional email notifications.
- **Microsoft SQL Server**: persistence for devices, measurements, and alarms.
- **Java 21+ and Spring Boot**: service runtime and application framework.

## Architecture

```mermaid
flowchart LR
    Producer[IoT device or producer]
    Measurement[Measurement Service\nREST :8080]
    Integration[Integration Service\nREST :8081]
    Device[Device Service\nREST :8082]
    Alarm[Alarm Service\nSOAP /ws :8083]
    SQL[(Microsoft SQL Server)]
    Mail[SMTP server]

    Producer -->|POST /api/measurements| Measurement
    Measurement -->|save measurement| SQL
    Measurement -->|POST /api/integration/measurements\nX-Correlation-ID| Integration
    Integration -->|GET /api/devices/{deviceId}| Device
    Integration -->|SOAP processMeasurement| Alarm
    Device -->|device data| SQL
    Alarm -->|alarm data| SQL
    Alarm -.->|optional notification| Mail
```

### Measurement flow

1. A producer submits a measurement to the Measurement Service.
2. The Measurement Service validates the request, generates a measurement ID and correlation ID, and saves the measurement with status `RECEIVED`.
3. The Measurement Service forwards the request to the Integration Service. A forwarding failure is logged, but the already-persisted measurement is still returned to the caller.
4. The Integration Service checks the device registry through the Device Service.
5. If the device exists, the Integration Service calls the Alarm Service through SOAP.
6. The Alarm Service evaluates the value against the configured rule.
7. If an alarm is triggered, it is stored as an active critical alarm and an email notification is attempted when SMTP settings are configured.

The correlation ID is passed through the REST and SOAP integration logs so that one measurement can be followed across services.

### Endpoint communication graph

The following graph shows the application-to-application calls and the URL that should be configured for each deployed service. Replace the placeholder host names with the actual Azure App Service names after deployment.

```mermaid
flowchart TD
  Client[IoT producer or API client]
  Measurement[Measurement Service\nhttps://<measurement-app>.azurewebsites.net]
  Integration[Integration Service\nhttps://<integration-app>.azurewebsites.net]
  Device[Device Service\nhttps://<device-app>.azurewebsites.net]
  Alarm[Alarm Service\nhttps://<alarm-app>.azurewebsites.net/ws]

  Client -->|POST /api/measurements| Measurement
  Measurement -->|POST /api/integration/measurements| Integration
  Integration -->|GET /api/devices/{deviceId}| Device
  Integration -->|SOAP processMeasurement| Alarm
```

## Repository Layout

```text
database/
  schema.sql                         Database schema location
docs/
  architecture.md                    Architecture documentation location
producers/
  water-leak                         Reserved producer location
services/
  device-service/                    Device registry REST service
  measurement-service/               Measurement ingestion REST service
  integration-service/               REST-to-service integration layer
  AlarmService/                      Alarm SOAP service
tests/
  error-test.md                      Error-test documentation location
```

## Services And Endpoints

### Measurement Service

Default port: `8080`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/measurements` | Store and forward a measurement |
| `GET` | `/api/measurements` | List measurements, newest first |
| `GET` | `/api/measurements/{measurementId}` | Retrieve one measurement by UUID |

Example request:

```json
{
  "deviceId": "humidity-01",
  "measurementType": "humidity",
  "value": 65,
  "unit": "%",
  "timestamp": "2026-09-16T12:00:00Z"
}
```

Example command:

```bash
curl -X POST http://localhost:8080/api/measurements \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "humidity-01",
    "measurementType": "humidity",
    "value": 65,
    "unit": "%",
    "timestamp": "2026-09-16T12:00:00Z"
  }'
```

The response contains the generated `measurementId`, the `correlationId`, the original measurement data, the normalized UTC timestamp, and the initial status.

### Device Service

Default port: `8082`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/devices` | Register a device |
| `GET` | `/api/devices` | List all registered devices |
| `GET` | `/api/devices/{deviceId}` | Retrieve a device by its external ID |

Register a device before submitting measurements for it:

```bash
curl -X POST http://localhost:8082/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "humidity-01",
    "name": "Humidity Sensor 01",
    "deviceType": "humidity-sensor",
    "location": "Server room"
  }'
```

Device IDs are unique. Registering the same ID twice returns a duplicate-device error.

### Integration Service

Default port: `8081`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/integration/measurements` | Validate the device and forward to Alarm Service |

The endpoint accepts the same measurement shape as the Measurement Service. It accepts an optional `X-Correlation-ID` header; when absent, the service generates one.

### Alarm Service

Default port: `8083`

Local SOAP endpoint: `http://localhost:8083/ws`

Azure App Service SOAP endpoint placeholder: `https://<alarm-app>.azurewebsites.net/ws`

Local WSDL: `http://localhost:8083/ws/alarm.wsdl`

Azure WSDL placeholder: `https://<alarm-app>.azurewebsites.net/ws/alarm.wsdl`

The Alarm Service publishes the shared SOAP contract under the namespace `http://iotmonitoring.com/alarm`:

- `processMeasurement`: evaluate and, when necessary, create an alarm.
- `getAlarm`: retrieve a persisted alarm by numeric ID.
- `getThreshold`: retrieve the configured threshold for a measurement type.

The contract files are located at:

- `services/AlarmService/src/main/resources/wsdl/alarm-service.wsdl`
- `services/AlarmService/src/main/resources/wsdl/alarm-service.xsd`

SOAP validation errors and missing alarms are mapped to the contract's `AlarmFault` detail.

## Alarm Rules

| Measurement type | Unit | Alarm condition |
| --- | --- | --- |
| `water_leak` | `boolean` | value equals `1` |
| `temperature` | `C` | value is greater than or equal to `30` |
| `humidity` | `%` | value is greater than or equal to `80` |
| `distance` | `cm` | value is less than or equal to `10` |

Alarm records are created with severity `CRITICAL` and status `ACTIVE`. Non-triggering measurements receive a successful response with `alarmCreated: false`.

## Data Model

The services use SQL Server with the `dbo` schema and Hibernate schema generation disabled. The application expects the database to contain these logical tables:

- **`devices`**: external device ID, name, type, and location.
- **`Measurements`**: measurement UUID, correlation UUID, device ID, type, value, unit, timestamp, and status.
- **`alarms`**: alarm ID, device ID, measurement data, severity, status, message, measurement timestamp, and creation timestamp.

The current `database/schema.sql` file is a placeholder, so database provisioning must be completed separately before running the services against SQL Server.

## Configuration

The services are configured through environment variables.

### Shared database settings

The Device Service and Alarm Service use:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

The Measurement Service uses:

```text
AZURE_SQL_URL
AZURE_SQL_USERNAME
AZURE_SQL_PASSWORD
```

### Service URLs

For Azure App Service deployment, replace the placeholder host names with the deployed App Service URLs:

```text
INTEGRATION_SERVICE_URL=https://<integration-app>.azurewebsites.net
DEVICE_SERVICE_URL=https://<device-app>.azurewebsites.net
ALARM_SERVICE_URL=https://<alarm-app>.azurewebsites.net/ws
```

For local development, use:

```text
INTEGRATION_SERVICE_URL=http://localhost:8081
DEVICE_SERVICE_URL=http://localhost:8082
ALARM_SERVICE_URL=http://localhost:8083/ws
```

`ALARM_SERVICE_URL` defaults to `http://localhost:8083/ws`. `DEVICE_SERVICE_URL` is required by the Integration Service and has no default.

### Azure App Service URL checklist

| Service | Azure App Service URL placeholder | Used by |
| --- | --- | --- |
| Measurement Service | `https://<measurement-app>.azurewebsites.net` | External producers and API clients |
| Integration Service | `https://<integration-app>.azurewebsites.net` | Measurement Service via `INTEGRATION_SERVICE_URL` |
| Device Service | `https://<device-app>.azurewebsites.net` | Integration Service via `DEVICE_SERVICE_URL` |
| Alarm Service | `https://<alarm-app>.azurewebsites.net/ws` | Integration Service via `ALARM_SERVICE_URL` |

When the services are deployed to Azure App Service, configure these values as App Service application settings rather than committing real URLs or credentials to source control.

### Email notifications

Email delivery is optional. Configure these variables to enable notifications:

```text
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=sender@example.com
MAIL_PASSWORD=your-password
MAIL_TO=alerts@example.com
```

When `MAIL_USERNAME` or `MAIL_TO` is blank, alarms are still stored but the email notification is skipped.

## Running Locally

Prerequisites:

- Java 21 or later.
- Maven 3.9+.
- A reachable Microsoft SQL Server database with the expected tables.
- SMTP credentials only if email notifications are required.

Open four terminals and start the services in this order:

```bash
# Device Service
cd services/device-service
./mvnw spring-boot:run

# Alarm Service
cd services/AlarmService
mvn spring-boot:run

# Integration Service
cd services/integration-service
./mvnw spring-boot:run

# Measurement Service
cd services/measurement-service
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` for the services that include a Maven wrapper. The Alarm Service currently relies on an installed Maven executable.

After startup, register a device and submit a measurement using the examples above. To verify alarm creation, submit a value at or above the humidity threshold:

```bash
curl -X POST http://localhost:8080/api/measurements \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "humidity-01",
    "measurementType": "humidity",
    "value": 82,
    "unit": "%",
    "timestamp": "2026-09-16T12:00:00Z"
  }'
```

The response from the Measurement Service confirms that the measurement was saved. The Alarm Service response is logged by the Integration Service and includes whether an alarm was created.

## Testing

Each service is an independent Maven project. Run tests from the relevant service directory:

```bash
./mvnw test
```

For the Alarm Service, use:

```bash
mvn test
```

The test suites include Spring context and service-level tests. The current repository does not include a Docker Compose environment or a complete automated end-to-end test harness, so full integration testing requires the services and SQL Server to be running.

## Design Decisions

- **REST at the system edge** keeps device registration and measurement ingestion easy to integrate with producers.
- **A dedicated Integration Service** isolates orchestration and cross-service communication from the Measurement Service.
- **SOAP for alarms** preserves a stable, shared contract between the Integration Service and Alarm Service.
- **Persist before forwarding** prevents a temporary downstream outage from losing the original measurement.
- **Correlation IDs** make it possible to trace a measurement through storage, validation, forwarding, and alarm evaluation.
- **Explicit thresholds** keep alarm behavior predictable and easy to demonstrate.

## Current Scope And Future Improvements

The repository is a functional service-layer prototype intended to demonstrate the complete measurement-to-alarm workflow. Possible next steps include:

- Populate and version the SQL Server schema script.
- Add Dockerfiles and a Docker Compose development environment.
- Add a real producer implementation under `producers/water-leak`.
- Add health checks, retry policies, and a durable message broker for asynchronous delivery.
- Add end-to-end tests covering the complete REST-to-SOAP flow.
- Add alarm resolution and acknowledgement operations.
- Externalize alarm thresholds into configuration or a database.
