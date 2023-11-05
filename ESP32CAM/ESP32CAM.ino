#include "mqtt.h"


MqttManager mqttManager;

void setup() {
  // Open the serial port for easy viewing
  Serial.begin(115200);

  mqttManager.setup();
}

void loop() {
  if (!mqttManager.client.connected()) {
    mqttManager.reconnect();
  }
  mqttManager.loop();
}
