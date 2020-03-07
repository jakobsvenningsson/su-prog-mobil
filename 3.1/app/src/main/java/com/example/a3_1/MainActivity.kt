package com.example.a3_1

import android.os.Bundle
import android.content.Intent
import android.Manifest.permission
import android.content.pm.PackageManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import android.graphics.Bitmap
import android.app.Activity
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val permissionReqCode = 100
    private val cameraReqCode = 101
    private var cameraBtn: Button? = null
    private var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.cameraBtn = findViewById<Button>(R.id.cameraBtn)
        this.cameraBtn?.setOnClickListener lambda@{
            if (!checkPermission((permission.CAMERA))) {
                requestPermission(permission.CAMERA)
                return@lambda
            }
            launchIntent()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != permissionReqCode) {
            return
        }
        launchIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != cameraReqCode || resultCode != Activity.RESULT_OK) {
            return
        }
        val photo = data?.extras?.get("data") as Bitmap?
        imageView?.setImageBitmap(photo)
    }

    private fun launchIntent() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, cameraReqCode)
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission), permissionReqCode
        )
    }
}
