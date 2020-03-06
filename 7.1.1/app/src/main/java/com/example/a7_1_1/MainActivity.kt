package com.example.a7_1_1

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val copyBtn = findViewById<Button>(R.id.copyBtn)
        val pasteBtn = findViewById<Button>(R.id.pasteBtn)
        val clearBtn = findViewById<Button>(R.id.clearBtn)


        val copyTextView = findViewById<EditText>(R.id.copyEditText)
        val pasteTextView = findViewById<TextView>(R.id.pasteTextView)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        copyBtn.setOnClickListener {
            Toast.makeText(this, "Text copied.", Toast.LENGTH_SHORT).show()
            val clip = ClipData.newPlainText("label", copyTextView.text);
            clipboard.setPrimaryClip(clip)
        }

        pasteBtn.setOnClickListener lambda@{
            if(!clipboard.hasPrimaryClip()) {
                Toast.makeText(this, "Nothing to paste...", Toast.LENGTH_SHORT).show()
                return@lambda
            }
            val item = clipboard.primaryClip?.getItemAt(0)
            pasteTextView.text = item?.text.toString()
        }

        clearBtn.setOnClickListener {
            Toast.makeText(this, "Clipboard cleared...", Toast.LENGTH_SHORT).show()
            clipboard.clearPrimaryClip()
        }
    }
}
