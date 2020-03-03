package com.example.a7_2_1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.a7_2_1.R.drawable.ic_launcher_background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    var notificationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun sendNotification(_view: View) {
        val btn = findViewById<Button>(R.id.notificationBtn)
        btn.isEnabled = false
        val toast = getToast()
        val alert = getAlert(notificationId++)
        val duration = 3L

        toast.show()

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("sleep", duration.toString())
            delay(duration * 1000)
            alert.show()
            btn.isEnabled = true
        }
    }

    private fun getToast(): Toast {
        val text = "Preparing notification..."
        val duration = Toast.LENGTH_SHORT
        return Toast.makeText(applicationContext, text, duration)
    }

    private fun getAlert(nId: Int): AlertDialog {
        val b = getNotification(nId)
        val posBtnAction: (DialogInterface, Int) -> Unit = {
            dialog: DialogInterface, _ ->
                with(NotificationManagerCompat.from(this)) {
                    notify(
                        nId,
                        b.build()
                    )
                }
                dialog.cancel()

            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(this)
        builder
            .setMessage("Do you really want to send a notification?")
            .setTitle("Send Notification?")
            .setPositiveButton("OK", posBtnAction)
            .setNegativeButton("Cancel") {
                    dialog, _ -> dialog.cancel()
            }
        return builder.create()
    }

    private fun getNotification(nId: Int): NotificationCompat.Builder {
        val intent = Intent(this, NotificationActivity::class.java).apply {
            putExtra("NOTIFICATION_ID", nId)
        }
        val pendingIntent = PendingIntent.getActivity(this,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, "0")
            .setContentTitle("New notification.")
            .setContentText("...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(ic_launcher_background)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "0"
            val descriptionText = """Test application."""
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance)
                .apply {
                    description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
