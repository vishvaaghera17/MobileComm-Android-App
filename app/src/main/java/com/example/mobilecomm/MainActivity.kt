package com.example.mobilecomm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnFirebase).setOnClickListener {
            startActivity(Intent(this, FirebaseNotificationActivity::class.java))
        }
        findViewById<Button>(R.id.btnRestApi).setOnClickListener {
            startActivity(Intent(this, RestApiActivity::class.java))
        }
        findViewById<Button>(R.id.btnBluetooth).setOnClickListener {
            startActivity(Intent(this, BluetoothChatActivity::class.java))
        }
    }
}