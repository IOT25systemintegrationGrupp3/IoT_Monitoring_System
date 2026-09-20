# Humidity producer – Hum-01

ESP32-producent som läser relativ luftfuktighet från en DHT22 och skickar mätningen som JSON med HTTP POST.

## Hårdvara

- ESP32 Dev Module
- DHT22-modul med tre anslutningar
- Dupontkablar

## Koppling

| DHT22 | ESP32 |
|---|---|
| `+` | `3V3` |
| `OUT` | `GPIO4` / `D4` |
| `-` | `GND` |

Koppla alltid bort USB-strömmen innan kablar ändras.

## Konfiguration

1. Kopiera `arduino_secrets.example.h` till `arduino_secrets.h`.
2. Fyll i Wi-Fi-namn, Wi-Fi-lösenord och API-adress i den lokala filen.
3. Lägg aldrig till `arduino_secrets.h` i Git. Filen finns i `.gitignore`.

Exempel på slutlig API-adress:

```text
https://SERVER/api/measurements
```

## Arduino IDE

1. Installera `esp32 by Espressif Systems` i Boards Manager.
2. Installera `DHT sensor library by Adafruit` inklusive beroenden.
3. Välj `ESP32 Dev Module` och rätt COM-port.
4. Öppna `humidity-producer-Hum-01.ino` och ladda upp.
5. Öppna Serial Monitor med `115200 baud`.

Om uppladdningen stannar vid `Connecting...`, håll in `BOOT` tills skrivningen börjar.

## JSON-format

```json
{
  "deviceId": "Hum-01",
  "measurementType": "humidity",
  "value": 56.1,
  "unit": "%",
  "timestamp": "2026-09-20T08:19:35Z"
}
```

En ny mätning skickas var 30:e sekund. Temperaturen skrivs enbart till Serial Monitor som diagnostik; REST-anropet skickar luftfuktigheten.
