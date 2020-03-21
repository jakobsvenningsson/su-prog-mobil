package com.example.a3_1

import android.Manifest.permission
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private val permissionReqCode = 100
    private val cameraReqCode = 101
    private var cameraBtn: Button? = null
    private var imageView: ImageView? = null
    private var bitmap: Bitmap? = null

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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable("BitmapImage", bitmap)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val image = savedInstanceState.getParcelable<Parcelable>("BitmapImage") as Bitmap?
        imageView?.setImageBitmap(image)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != permissionReqCode) return
        launchIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != cameraReqCode || resultCode != Activity.RESULT_OK) {
            return
        }
        bitmap = data?.extras?.get("data") as Bitmap?
        imageView?.setImageBitmap(bitmap)
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
