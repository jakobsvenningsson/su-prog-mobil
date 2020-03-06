package com.example.a6_2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.provider.Telephony
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity()  {
    private val PERMISSION_REQUEST_CODE = 100
    var sendBtn: Button? = null
    var smsManager: SmsManager? = null
    var numberEditText: EditText? = null
    var messageEditText: EditText? = null
    var lv: ListView? = null
    var receiver: BroadcastReceiver? = null
    private val messages = mutableListOf<Map<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        this.numberEditText = findViewById<EditText>(R.id.numberEditText)
        this.messageEditText = findViewById<EditText>(R.id.messageEditText)
        this.sendBtn = findViewById<Button>(R.id.sendBtn)
        this.smsManager = SmsManager.getDefault()
        this.lv = findViewById<ListView>(R.id.msgListView)
        this.lv?.adapter = SimpleAdapter(
            this,
            this.messages,
            android.R.layout.simple_list_item_2,
            arrayOf("sender", "body"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        sendBtn?.setOnClickListener lambda@{
            if(!checkPermission((Manifest.permission.SEND_SMS))) {
                requestPermission(Manifest.permission.SEND_SMS)
                return@lambda
            }

            val message = this.messageEditText?.text.toString()
            val number = this.numberEditText?.text.toString()

            if(message.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Invalid form.", Toast.LENGTH_SHORT).show()
                return@lambda
            }

            Log.d("sending to:", number)

            this.smsManager?.sendTextMessage(
                 number,
                null,
                 message,
                null,
                null
            )

            this.messageEditText?.text?.clear()
            this.numberEditText?.text?.clear()

            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
        }

        this.receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                smsMessages.forEach {
                    val body = it.messageBody ?: "No message body."
                    val sender = it.originatingAddress ?: "No sender info."
                    val e = mapOf(Pair("sender", sender), Pair("body", body))
                    updateListView(e)
                }
            }
        }

        if(!checkPermission((Manifest.permission.SEND_SMS))) {
            requestPermission(Manifest.permission.SEND_SMS)
        }

        if(!checkPermission((Manifest.permission.RECEIVE_SMS))) {
            requestPermission(Manifest.permission.RECEIVE_SMS)
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(this.receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(this.receiver)
    }

    private fun updateListView(e: Map<String, String>) {
        this.messages.add(0, e)
        (this.lv?.adapter as BaseAdapter).notifyDataSetChanged()
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return
        }
    }
}
