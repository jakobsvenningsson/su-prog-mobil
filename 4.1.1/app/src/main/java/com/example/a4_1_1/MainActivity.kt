package com.example.a4_1_1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val video1Btn = findViewById<Button>(R.id.video1Btn)
        val video2Btn = findViewById<Button>(R.id.video2Btn)
        val video3Btn = findViewById<Button>(R.id.video3Btn)

        video1Btn.setOnClickListener {
            val url = "https://www.youtube.com/watch?v=EfVTDEWHr5o"
            openVideo(url)
        }

        video2Btn.setOnClickListener {
            val url = "https://www.youtube.com/watch?v=ljNISJluEHA"
            openVideo(url)
        }

        video3Btn.setOnClickListener {
            val url = "https://www.youtube.com/watch?v=ycWtsOxLl6Q&t=208s"
            openVideo(url)
        }
    }

    private fun openVideo(url: String) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        this.startActivity(webIntent)
    }
}
