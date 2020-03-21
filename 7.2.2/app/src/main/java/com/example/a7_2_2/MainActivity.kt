package com.example.a7_2_2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat


/*
 * I was not able to test this assignment since
 * it requires a physical android device which I
 * do not have access to.
 */

class MainActivity : AppCompatActivity() {
    private val permissionVibeCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun vibrate(_view: View) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibEffect = VibrationEffect
                    .createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } else {
            Log.v("vibrate", "no vibrator on device.")
        }
    }
}
