# Humidity Producer – Hum-01

IoT-producent som mäter relativ luftfuktighet med en DHT22-sensor och en ESP32. Mätningen skickas som JSON med HTTPS POST till gruppens Measurement Service och kan därefter visas i dashboarden.

```text
DHT22 → ESP32 (Hum-01) → REST/JSON → Measurement Service → Databas → Dashboard
```

ESP32-programmet är skrivet i C++ med Arduino-ramverket. Servern kan vara skriven i exempelvis Java eftersom delarna kommunicerar genom ett gemensamt REST-API och JSON-format.

## Hårdvara och koppling

- ESP32 Dev Module
- DHT22-modul
- Tre Dupontkablar
- USB-kabel
- Wi-Fi med 2,4 GHz

| DHT22 | ESP32 |
|---|---|
| `+` | `3V3` |
| `OUT` | `GPIO4` / `D4` |
| `-` | `GND` |

Koppla bort USB-strömmen innan kablarna flyttas.

## Projektfiler

| Fil | Innehåll |
|---|---|
| `humidity-producer-Hum-01.ino` | Programmet för ESP32 |
| `arduino_secrets.example.h` | Exempel på lokal konfiguration |
| `arduino_secrets.h` | Wi-Fi och API-adress; ignoreras av Git |
| `.gitignore` | Hindrar hemligheter från att laddas upp |

## Konfiguration

Installera följande i Arduino IDE:

1. `esp32 by Espressif Systems` i Boards Manager.
2. `DHT sensor library by Adafruit` i Library Manager.
3. Välj `ESP32 Dev Module` och rätt COM-port.

Kopiera `arduino_secrets.example.h`, döp kopian till `arduino_secrets.h` och fyll i:

```cpp
#pragma once

#define WIFI_SSID "DITT_WIFI_NAMN"
#define WIFI_PASSWORD "DITT_WIFI_LOSENORD"
#define API_URL "https://SERVER/api/measurements"
```

`arduino_secrets.h` ska aldrig laddas upp till GitHub.

## Hur programmet fungerar

### Bibliotek och inställningar

```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <DHT.h>
#include <time.h>
#include "arduino_secrets.h"

#define DHT_PIN 4
#define DHT_TYPE DHT22

const char* DEVICE_ID = "Hum-01";
const unsigned long SEND_INTERVAL_MS = 30000;
```

Biblioteken hanterar Wi-Fi, HTTPS, DHT22 och tid. Sensorn använder GPIO4, enheten identifieras som `Hum-01` och mätningar skickas var 30:e sekund.

### `createTimestamp()`

```cpp
String createTimestamp()
```

Hämtar aktuell tid och formaterar den som ISO 8601 i UTC, exempelvis:

```text
2026-09-24T12:30:45Z
```

Om tiden inte kan hämtas returneras en tom text och den aktuella mätningen skickas inte.

### `connectToWifi()`

```cpp
WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
```

Ansluter ESP32 till nätverket med uppgifterna från `arduino_secrets.h`. Funktionen väntar tills anslutningen lyckas och skriver sedan ut enhetens lokala IP-adress.

### `setup()`

`setup()` körs en gång när ESP32 startar:

1. Serial Monitor startas med `115200 baud`.
2. DHT22-sensorn startas.
3. ESP32 ansluter till Wi-Fi.
4. UTC-tid konfigureras genom NTP-servrar.

### `loop()`

`loop()` körs om och om igen:

1. Kontrollerar Wi-Fi och återansluter vid behov.
2. Läser luftfuktighet och temperatur från DHT22.
3. Avbryter mätningen om sensorn lämnar ett ogiltigt värde.
4. Hämtar en tidsstämpel.
5. Skapar ett JSON-meddelande.
6. Skickar JSON med HTTPS POST.
7. Skriver ut serverns HTTP-status.
8. Väntar 30 sekunder och börjar om.

Temperaturen visas endast som diagnostik i Serial Monitor. Det är luftfuktigheten som skickas till servern.

## JSON-format

```json
{
  "deviceId": "Hum-01",
  "measurementType": "humidity",
  "value": 58.40,
  "unit": "%",
  "timestamp": "2026-09-24T12:30:45Z"
}
```

| Fält | Betydelse |
|---|---|
| `deviceId` | Identifierar sensorn |
| `measurementType` | Anger att luftfuktighet mäts |
| `value` | Uppmätt värde |
| `unit` | Procent |
| `timestamp` | Tidpunkt för mätningen |

## HTTPS och POST

```cpp
WiFiClientSecure client;
client.setInsecure();

HTTPClient http;
http.begin(client, API_URL);
http.addHeader("Content-Type", "application/json");
int responseCode = http.POST(json);
```

`HTTPClient` skickar mätningen till REST-tjänsten. `Content-Type` visar att innehållet är JSON. `setInsecure()` används för skoldemonstrationen och innebär att ESP32 inte verifierar servercertifikatet. En produktionslösning bör i stället verifiera rätt certifikat.

## Uppladdning och test

1. Anslut ESP32 med USB.
2. Öppna `.ino`-filen i Arduino IDE.
3. Välj `ESP32 Dev Module` och rätt COM-port.
4. Klicka på Upload.
5. Håll inne `BOOT` om uppladdningen stannar på `Connecting...`.
6. Öppna Serial Monitor med `115200 baud`.

Vid ett lyckat test visas ungefär:

```text
Wi-Fi anslutet!
Hum-01 är redo.
Skickar JSON:
{"deviceId":"Hum-01","measurementType":"humidity","value":58.40,"unit":"%","timestamp":"..."}
HTTP-status: 201
```

`201 Created` betyder att servern tog emot mätningen. Kontrollera därefter att `Hum-01` syns i dashboarden. För att testa en förändring kan man andas försiktigt nära sensorn och vänta på nästa mätning.

## Enkel felsökning

| Resultat | Betydelse |
|---|---|
| `201` | Mätningen skapades korrekt |
| `400` | JSON eller något värde godkändes inte |
| `404` | API-adressen eller endpointen är fel |
| `500` | Fel i servern eller databasen |
| `-1` | ESP32 kunde inte nå servern |

Om DHT22 inte kan läsas, kontrollera kablarna och att `OUT` är ansluten till GPIO4. Om Wi-Fi inte fungerar, kontrollera nätverksnamn, lösenord och att nätverket använder 2,4 GHz.

## Sammanfattning

`Hum-01` läser luftfuktighet från en DHT22, skapar ett tidsstämplat JSON-meddelande och skickar det med HTTPS POST till gruppens Measurement Service. Processen upprepas var 30:e sekund och resultatet kan visas i dashboarden.
