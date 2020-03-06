package com.example.a4_1_2

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val googleBtn = findViewById<Button>(R.id.googleBtn)
        val amazonBtn = findViewById<Button>(R.id.amazonBtn)
        val redditBtn = findViewById<Button>(R.id.redditBtn)

        this.webView = findViewById<WebView>(R.id.webView)
        this.webView?.settings?.javaScriptEnabled = true

        googleBtn.setOnClickListener {
            load("https://www.google.com")
        }
        amazonBtn.setOnClickListener {
            load("https://www.amazon.com")
        }
        redditBtn.setOnClickListener {
            load("https://www.reddit.com")
        }

        load("https://www.google.com")
    }

    private fun load(url: String) {
        if(!checkPermission((Manifest.permission.INTERNET))) {
            requestPermission(Manifest.permission.INTERNET)
            return
        }
        this.webView?.loadUrl(url);
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), PERMISSION_REQUEST_CODE)
    }
}
