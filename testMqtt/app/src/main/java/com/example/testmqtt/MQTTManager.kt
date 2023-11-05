package com.example.testmqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.util.*


class MQTTManager(
    private val context: Context,
    private val host: String,
    private val userName: String?,
    private val password: String?,
    private val clientId: String,
    private val caCertResId: Int
) {

    private val persistence = MemoryPersistence()
    private var client: MqttAsyncClient? = null

    private fun createSslOptions(): MqttConnectOptions {
        val options = MqttConnectOptions()
        if (userName != null && password != null) {
            options.userName = userName
            options.password = password.toCharArray()
        }
        options.isCleanSession = true
        val fis = context.resources.openRawResource(caCertResId)

        val cf = CertificateFactory.getInstance("X.509")
        val ca = cf.generateCertificate(fis)

        val ks: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("caCert", ca)

        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, tmf.trustManagers, null)

        options.socketFactory = sslContext.socketFactory

        return options
    }

    // text message receiving callback function
    private var messageArrivedCallback: ((String, String) -> Unit)? = null
    fun setCallback(callback: (String, String) -> Unit) {
        messageArrivedCallback = callback
    }
    //　picture message receiving callback function
    private var imageArrivedCallback: ((String, MqttMessage?) -> Unit)? = null
    fun setImageCallback(callback: (String, MqttMessage?) -> Unit) {
        imageArrivedCallback = callback
    }

    @Throws(MqttException::class)
    fun connect() {
        client = MqttAsyncClient(host, clientId, persistence)


        client?.setCallback(object : MqttCallback{
            override fun connectionLost(cause: Throwable?) {
                // disconnected reconnection
                println("Connection Lost")
                reconnect()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val receivedTopic = topic ?: ""
                if (receivedTopic == "img") {
                    imageArrivedCallback?.invoke(receivedTopic, message)
                } else {
                    val payload = message?.toString() ?: ""
                    messageArrivedCallback?.invoke(receivedTopic, payload)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // If you need to confirm that the message publishing has been completed, you can handle it here
                println("message sent")
            }
        })


        val connOpts = createSslOptions()
        client?.connect(connOpts, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                println("Connected to MQTT broker")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                println("Failed to connect to MQTT broker")
            }
        })

    }


    fun reconnect() {
        if (!client?.isConnected!!) {
            try {
                connect()
            } catch(e: MqttException) {
                Log.e("MQTT", "Error reconnecting: ", e)
            }
        }
    }

    @Throws(MqttException::class)
    fun publish(topic: String, payload: String, qos: Int) {
        val message = MqttMessage(payload.toByteArray(charset("utf-8")))
        message.qos = qos
        client?.publish(topic, message, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                println("Message published")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                println("Failed to publish message")
            }
        })
    }


    @Throws(MqttException::class)
    fun subscribe(topic: String, qos: Int) {
        client?.subscribe(topic,qos,null,object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                println("Subscribed to topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                println("Failed to subscribe topic")
            }
        })
    }
}
