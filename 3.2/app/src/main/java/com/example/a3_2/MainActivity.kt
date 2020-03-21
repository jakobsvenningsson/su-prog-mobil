package com.example.a3_2

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.media.MediaRecorder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException

/*
 * The MediaRecorder API does not work on the android emulator.
 * There I have not been able to properly test this assignment
 * since I do not have access to a android device. Keep this in
 * mind when correcting.
 */

class MainActivity : AppCompatActivity() {
    private var playBtn: Button? = null
    private var recordBtn: Button? = null
    private var stopBtn: Button? = null
    private var audioFile: File? = null
    private val recorder = MediaRecorder()
    private val reqCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.playBtn = findViewById(R.id.playBtn)
        this.recordBtn = findViewById(R.id.recordBtn)
        this.stopBtn = findViewById(R.id.stopBtn)

        this.stopBtn?.isEnabled = false
        this.playBtn?.isEnabled = false

        if(!checkPermission((Manifest.permission.RECORD_AUDIO))) {
            requestPermission(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun record(view: View) {
        this.stopBtn?.isEnabled = true
        this.playBtn?.isEnabled = false
        this.recordBtn?.isEnabled = false

        try {
            recorder.prepare()
            recorder.start()
        } catch(e: Exception) {
            Log.d("record", "Exception when trying to start recording.")
            e.printStackTrace()
        }
    }

    fun stop(view: View) {
        this.stopBtn?.isEnabled = false
        this.playBtn?.isEnabled = true
        this.recordBtn?.isEnabled = true

        try {
            recorder.stop()
            recorder.release()
        } catch(e: Exception) {
            Log.d("play", "Exception when trying to stop recording.")
            e.printStackTrace()
        }
    }

    fun play(view: View) {
        if(this.audioFile == null) {
            Toast.makeText(this, "No recording.", Toast.LENGTH_SHORT)
            return
        }
        val uri = Uri.parse("file://" + this.audioFile?.absolutePath)
        try {
            MediaPlayer.create(this, uri).apply {
                start()
            }
        } catch(e: Exception) {
            Log.d("play", "Exception when trying to play recording.")
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != this.reqCode) {
            return
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            this.audioFile = File.createTempFile("sound", ".3gp", this.cacheDir)
        } catch (e: IOException) {
            Log.e("onCreate", "external storage access error")
            return
        }
        recorder.setOutputFile(this.audioFile);
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), reqCode)
    }
}
