package com.example.a7_2_1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        val nId = intent.getIntExtra("NOTIFICATION_ID", -1)
        val view = findViewById<TextView>(R.id.notification_id_view)
        view.text = nId.toString()
    }
}
