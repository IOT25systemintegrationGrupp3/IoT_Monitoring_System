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

DHT dht(DHT_PIN, DHT_TYPE);

String createTimestamp() {
  struct tm timeInfo;

  if (!getLocalTime(&timeInfo, 10000)) {
    return "";
  }

  char timestamp[25];
  strftime(timestamp, sizeof(timestamp), "%Y-%m-%dT%H:%M:%SZ", &timeInfo);
  return String(timestamp);
}

void connectToWifi() {
  Serial.print("Ansluter till Wi-Fi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("Wi-Fi anslutet!");
  Serial.print("IP-adress: ");
  Serial.println(WiFi.localIP());
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  dht.begin();
  connectToWifi();

  // UTC-tid används i JSON-fältet timestamp.
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");

  Serial.println("Hum-01 är redo.");
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi tappades. Ansluter igen...");
    connectToWifi();
  }

  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Kunde inte läsa DHT22.");
    delay(5000);
    return;
  }

  String timestamp = createTimestamp();

  if (timestamp == "") {
    Serial.println("Kunde inte hämta aktuell tid.");
    delay(5000);
    return;
  }

  String json = "{";
  json += "\"deviceId\":\"";
  json += DEVICE_ID;
  json += "\",";
  json += "\"measurementType\":\"humidity\",";
  json += "\"value\":";
  json += String(humidity, 2);
  json += ",";
  json += "\"unit\":\"%\",";
  json += "\"timestamp\":\"";
  json += timestamp;
  json += "\"";
  json += "}";

  Serial.println("Skickar JSON:");
  Serial.println(json);

  WiFiClientSecure client;

  // För skoldemonstrationen. Produktionskod bör verifiera servercertifikatet.
  client.setInsecure();

  HTTPClient http;

  if (http.begin(client, API_URL)) {
    http.addHeader("Content-Type", "application/json");

    int responseCode = http.POST(json);
    Serial.print("HTTP-status: ");
    Serial.println(responseCode);

    if (responseCode < 200 || responseCode >= 300) {
      Serial.print("Svar: ");
      Serial.println(http.getString());
    }

    http.end();
  } else {
    Serial.println("Kunde inte starta HTTP-anropet.");
  }

  Serial.print("Temperatur (diagnostik): ");
  Serial.print(temperature);
  Serial.println(" C");

  delay(SEND_INTERVAL_MS);
}
