package com.example.a3_3

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import android.provider.MediaStore
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.widget.VideoView
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val permissionCameraCode = 100
    private val permissionStorageCode = 101
    private val videoReqCode = 102
    private var recordBtn: Button? = null
    private var videoView: VideoView? = null
    private var videoFile: File? = null
    private var uri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.videoView = findViewById(R.id.videoView)
        videoView?.setOnCompletionListener {
            videoView?.start()
        }

        this.recordBtn = findViewById(R.id.recordBtn)
        this.recordBtn?.setOnClickListener lambda@{
            if(!checkPermission((Manifest.permission.CAMERA))) {
                requestPermission(Manifest.permission.CAMERA, this.permissionCameraCode)
                return@lambda
            }
            startVideo()
        }

        try {
            this.videoFile = File.createTempFile("sound", ".mp4", this.cacheDir)
        } catch (e: IOException) {
            return
        }

        if(!checkPermission((Manifest.permission.READ_EXTERNAL_STORAGE))) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, this.permissionStorageCode)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable("videoUri", this.uri)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val uri = savedInstanceState.getParcelable<Parcelable>("videoUri") as Uri?
        videoView?.setVideoURI(uri)
        videoView?.start()
    }

    private fun startVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.videoFile)
        startActivityForResult(intent, videoReqCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        when(requestCode) {
            this.permissionCameraCode->
                if(permGranted) startVideo()
            else ->
                return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != videoReqCode || resultCode != Activity.RESULT_OK) {
            return
        }
        this.uri = data?.data
        videoView?.setVideoURI(data?.data)
        videoView?.start()
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), reqCode)
    }
}
