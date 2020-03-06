package com.example.a4_1_3

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.app.ActivityCompat
import android.widget.Toast
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUEST_CODE = 101
    private var fileUri: Uri? = null

    var receiverEditText: EditText? = null
    var subjectEditText: EditText? = null
    var messageEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.receiverEditText = findViewById<EditText>(R.id.receiverEditText)
        this.subjectEditText = findViewById<EditText>(R.id.subjectEditText)
        this.messageEditText = findViewById<EditText>(R.id.messageEditText)

        if(!checkPermission((Manifest.permission.READ_EXTERNAL_STORAGE))) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun sendEmail(view: View) {
        val receiverIsEmpty = this.receiverEditText?.text?.isEmpty() ?: true
        val subjectIsEmpty = this.subjectEditText?.text?.isEmpty() ?: true
        val msgIsEmpty = this.messageEditText?.text?.isEmpty() ?: true
        if(receiverIsEmpty || subjectIsEmpty || msgIsEmpty) {
            Toast.makeText(this, "Invalid form.", Toast.LENGTH_SHORT).show()
            return
        }

        val receiver = receiverEditText?.text.toString()
        val subject = subjectEditText?.text.toString()
        val message = messageEditText?.text.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(receiver))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            this@MainActivity.fileUri?.let { fileUri ->
                putExtra(Intent.EXTRA_STREAM, fileUri)
            }
        }

        try {
            startActivity(Intent.createChooser(intent, "send mail using:"));
        } catch(e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No handler found.",
                Toast.LENGTH_SHORT
            ).show()
        }

        receiverEditText?.text?.clear()
        subjectEditText?.text?.clear()
        messageEditText?.text?.clear()
    }

    fun addAttachment(view: View) {
        if(!checkPermission((Manifest.permission.READ_EXTERNAL_STORAGE))) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        this.startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        data?.data?.let { uri ->
            val fName = getFileNameFromUri(uri)
            val attachmentTextView = findViewById<TextView>(R.id.attachmentTextView)
            attachmentTextView.text = fName
            this.fileUri = uri
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(
             uri,
            null,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
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
