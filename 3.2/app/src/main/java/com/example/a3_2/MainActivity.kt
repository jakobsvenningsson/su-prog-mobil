package com.example.a3_2

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var playBtn: Button? = null
    private var recordBtn: Button? = null
    private var stopBtn: Button? = null
    private var audioFile: File? = null
    private var recorder: MediaRecorder? = null
    private val reqCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.playBtn = findViewById(R.id.playBtn)
        this.recordBtn = findViewById(R.id.recordBtn)
        this.stopBtn = findViewById(R.id.stopBtn)

        this.stopBtn?.isEnabled = false
        this.playBtn?.isEnabled = false
        this.recordBtn?.isEnabled = true
    }

    fun record(view: View) {
        if(!checkPermission((Manifest.permission.RECORD_AUDIO))) {
            requestPermission(Manifest.permission.RECORD_AUDIO)
            return
        }
        this.stopBtn?.isEnabled = true
        this.playBtn?.isEnabled = false
        this.recordBtn?.isEnabled = false

        try {
            this.audioFile = File.createTempFile("sound", ".3gp", this.cacheDir)
        } catch (e: IOException) {
            Log.e("onCreate", "external storage access error")
            return
        }

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            if(Build.VERSION.SDK_INT < 26) {
                setOutputFile(audioFile?.getAbsolutePath());
            } else {
                setOutputFile(audioFile);
            }
        }

        try {
            recorder?.prepare()
            recorder?.start()
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
            recorder?.stop()
            recorder?.release()
            recorder = null
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
            Log.d("play", "Played: " + uri.toString())

        } catch(e: Exception) {
            Log.d("play", "Exception when trying to play recording.")
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != this.reqCode) {
            return
        }
        this.recordBtn?.isEnabled = true
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
