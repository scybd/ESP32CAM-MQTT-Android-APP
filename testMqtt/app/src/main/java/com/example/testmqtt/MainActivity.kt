package com.example.testmqtt

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")

    lateinit var imageView: ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        imageView = findViewById(R.id.image)

        val mqttManager = MQTTManager(
            context = this,
            host = "ssl://x64554c8.ala.cn-hangzhou.emqxsl.cn:8883",
            userName = "t",
            password = "2",
            clientId = "android",
            caCertResId = R.raw.emqxsl_ca
        )

        // Update subscribed messages to TextView
        val messageReceived = findViewById<TextView>(R.id.tvMessage)
        mqttManager.setCallback{topic, message ->
            runOnUiThread{
                messageReceived.text = "Topic: $topic Message: $message"
            }
        }
        //　Update subscribed image messages to ImageView
        mqttManager.setImageCallback{topic, message ->
            val bytes = message?.payload
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes?.size?: 0)
            runOnUiThread{
                imageView.setImageBitmap(bitmap)
            }
        }

        // connect to mqtt broker
        try {
            mqttManager.connect()
            Thread.sleep(2000)      // delay to ensure it connect to mqtt broker
            mqttManager.subscribe("text", 0)
            mqttManager.subscribe("img", 0)
            
        } catch (ex:Exception) {
            Log.d("connect", ex.message.toString())
        }

        // events for btnTest
        val btnTest: MaterialButton = findViewById(R.id.btnTest)
        btnTest.setOnClickListener {
            try {
                mqttManager.publish("btnTest", "btn_messge", 0)
            }catch (ex:Exception){
                Log.d("publish", ex.message.toString())
            }
        }

    }
}