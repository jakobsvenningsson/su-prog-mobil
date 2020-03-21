package com.example.a1_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import org.w3c.dom.Text

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class MainActivity : AppCompatActivity() {

    private var btn: Button ? = null
    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById<Button>(R.id.button)
        btn?.isEnabled = false
        editText = findViewById<EditText>(R.id.editText)

        // Add listener for text changes to enable/disable button when text field is empty/contains a word.
        // This prevents user's from navigating to the DisplayMessageActivity with an empty string.
        // This mechanism is not part of the android tutorial but I felt like improving it a bit :)
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btn?.isEnabled = count > 0
            }
        })
    }

    fun sendMessage(view: View) {
        val msg = editText?.text?.toString()
        val intent = Intent(this, DisplayMessageActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, msg)
        }
        startActivity(intent)
    }
}
