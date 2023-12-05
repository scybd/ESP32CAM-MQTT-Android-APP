# ESP32CAM + Android APP(基于kotlin)的MQTT远程数据(图像)收发demo

## 演示效果
[video](https://www.bilibili.com/video/BV1vC4y1J733/?vd_source=7d3b4f31afe705472aae6df46494b6a0)

## 所需环境
### 硬件
- 一块esp32cam核心板，一块esp32cam的下载底板 ![ESP32CAM](/img/ESP32CAM.jpg)
- 一根microUSB数据线
- 一部安卓手机

### 软件
- Arduino IDE
- Android Studio


## 使用说明
1. 该demo中我使用的是免费的[EMQX Cloud](https://www.emqx.com/zh/cloud)服务器，在创建了自己的服务器后可以看到其信息：
    > ![EMQX Cloud Message](/img/EMQX%20Cloud%20Message.jpg)

2. 我们还需要添加认证，我的用户名为**t**，密码为**2**：
    > ![EMQX Cloud Identification](/img/identification.jpg)

3. 通过**Arduino IDE**打开ESP32CAM文件夹中的ESP32CAM.ino，注意修改**mqtt.h**中MqttManager类的私有变量**ssid**和**password**，他们分别为你的ESP32CAM将要连接的wifi(2.4GHz频段)名称和密码。此外还需要修改下面MQTT云服务器的一些参数再下载代码到esp32cam：
    | 参数 | 说明 |
    | :---: | :---------: |
    | mqtt_broker | 云服务器的连接地址 |
    | topic | esp32cam订阅的话题（demo中并未使用到，可以按需添加其用途）|
    | mqtt_username | 认证的用户名 |
    | mqtt_password | 认证的密码 |
    | mqtt_port | MQTT端口 |
    | ca_cert | CA证书，需要下载后用记事本打开并黏贴到代码中（kotlin的话需要将证书文件黏贴到res/raw/目录下） |
   
4. 通过**Android Studio**打开testMqtt，我们也需要在**MainActivity.kt**修改其中MQTT云服务器的对应参数再部署到手机上。


## 注意事项
### ESP32CAM
- mqtt通信协议是TCP协议的一层封装，我们在使用WiFiClientSecure库的同时也需要使用官方封装好的mqtt的库**PubSubClient**。不过该库的原代码中对一个数据包的大小做了限制，事实上，MQTT本身就不能传输过大的消息(我记得是256KB)。所以为了传输图像我们需要更改**PubSubClient.h**中的宏定义**MQTT_MAX_PACKET_SIZE**为50000(你可以尝试更大的值，不过esp32cam只有520KB的SRAM)。

### APP
- 整个工程中的核心是**MainActivity.kt**，**MQTTManager.kt**，**main_layout.xml**和**raw/emqxsl_ca.crt**
    > ![tree](/img/tree.png)
