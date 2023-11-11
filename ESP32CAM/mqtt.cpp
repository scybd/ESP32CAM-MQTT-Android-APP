#include "mqtt.h"


int last_capture_time = 0;
int delay_between_captures = 100;   // send images every 100ms
static int sendNum = 0;

MqttManager::MqttManager(): client(espClient)
{

}


void MqttManager::setup(){
  // connect to wifi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  // set camera parameters
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = 5;
  config.pin_d1 = 18;
  config.pin_d2 = 19;
  config.pin_d3 = 21;
  config.pin_d4 = 36;
  config.pin_d5 = 39;
  config.pin_d6 = 34;
  config.pin_d7 = 35;
  config.pin_xclk = 0;
  config.pin_pclk = 22;
  config.pin_vsync = 25;
  config.pin_href = 23;
  config.pin_sscb_sda = 26;
  config.pin_sscb_scl = 27;
  config.pin_pwdn = 32;
  config.pin_reset = -1;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_QVGA;
  config.jpeg_quality = 20;
  config.fb_count = 1;

  // initialize camera
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  // set root ca cert
  espClient.setCACert(ca_cert);

  // connect to mqtt broker
  client.setServer(mqtt_broker, mqtt_port);
  client.setCallback(callback);
  while (!client.connected()) {
    String client_id = "esp32";
    Serial.printf("The client %s connects to the public mqtt broker\n", client_id.c_str());
    if (client.connect(client_id.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("Public emqx mqtt broker connected");
    } else {
      Serial.print("Failed to connect to MQTT broker, rc=");
      Serial.print(client.state());
      Serial.println("Retrying in 5 seconds.");
      delay(5000);
    }
  }
  // publish and subscribe
  client.subscribe(topic);
}


void MqttManager::callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
  Serial.println("-----------------------");
}


void MqttManager::reconnect() {
  // set root ca cert
  espClient.setCACert(ca_cert);
  // connect to mqtt broker
  client.setServer(mqtt_broker, mqtt_port);
  client.setCallback(callback);
  while (!client.connected()) {
   String client_id = "esp32";
    Serial.printf("The client %s connects to the public mqtt broker\n", client_id.c_str());
    if (client.connect(client_id.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("Public emqx mqtt broker connected");
    } else {
      Serial.print("Failed to connect to MQTT broker, rc=");
      Serial.print(client.state());
      Serial.println("Retrying in 5 seconds.");
      delay(5000);
    }
  }
}


void MqttManager::publish(char* topic, char* message)
{
  client.publish(topic, message);
}

void MqttManager::publish(char* topic, uint8_t* payload, unsigned int length)
{
  if(client.publish(topic, payload, length))
  {
    Serial.println("Publish img");
  }
  else
  {
    Serial.print("Publish Failed, possible reasons are: ");
    if(!client.connected())
      Serial.println("Connection Lost");
    else if(client.state() != MQTT_CONNECTED)
      Serial.println("Not Connected To Broker");
    else if(!client.loop())
      Serial.println("Client Loop Failed, Network Error");
    else if(length + length > MQTT_MAX_PACKET_SIZE)
      Serial.println("Payload > MQTT_MAX_PACKET_SIZE");
  }
}


void MqttManager::loop()
{
  unsigned long current_time = millis();

  if (current_time - last_capture_time > delay_between_captures)
  {
    last_capture_time = current_time;

    // get image
    camera_fb_t * fb = NULL;
    fb = esp_camera_fb_get();
    if (!fb) {
      Serial.println("Camera capture failed");
      return;
    }

    // Convert images to byte stream and publish
    publish("img", (uint8_t*)fb->buf, fb->len);

    // Convert numbers to text and publish
    char sendNumBuf[10];
    sprintf(sendNumBuf, "%d", sendNum);
    publish("text", sendNumBuf);
    sendNum += 1;
    
    // Release camera image resources
    esp_camera_fb_return(fb);
  }
  
  client.loop();
}
