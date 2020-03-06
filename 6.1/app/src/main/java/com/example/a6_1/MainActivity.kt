package com.example.a6_1

import android.Manifest.permission
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.provider.ContactsContract
import android.widget.TextView
import android.widget.SimpleAdapter
import android.content.Intent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri






class MainActivity : AppCompatActivity() {
    var lv: ListView? = null
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize table view
        initTableView()

        // Setup contact list
        loadTableView()

        // Check if permission for making phone calls has been granted.
        if(!checkPermission((permission.CALL_PHONE))) {
            requestPermission(permission.CALL_PHONE)
        }
    }

    private fun loadTableView() {
        if (checkPermission(permission.READ_CONTACTS)) {
            loadContacts()
        } else {
            requestPermission(permission.READ_CONTACTS)
        }
    }

    private fun loadContacts() {
        val contacts = getContacts()
        val adapter = SimpleAdapter(
            this,
            contacts,
            android.R.layout.simple_list_item_2,
            arrayOf("name", "number"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        this.lv?.adapter = adapter
    }

    private fun getContacts(): List<Map<String, String>> {
        val contacts = ArrayList<Map<String, String>>()
        val cr = contentResolver
        var cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        ) ?: return emptyList()

        while(cur.moveToNext()){
            val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
            val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val hasNumber = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

            if(hasNumber == 0) {
                // Has no phone number
                continue
            }

            val pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                 arrayOf(id),
                null) ?: continue

            while (pCur.moveToNext()) {
                val number = pCur.getString(
                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                contacts.add(mapOf(Pair("name", name), Pair("number", number)))
                break
            }
            pCur.close()
        }
        cur?.close()

        return contacts
    }

    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return
        }
        val permGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        Toast.makeText(this, "Permission %s.".format(if(permGranted) "granted" else "not granted"),
            Toast.LENGTH_LONG).show()
        loadTableView()
    }

    private fun initTableView() {
        this.lv = findViewById<ListView>(R.id.listView)

        // Set view which will be displayed when list view is empty
        val emptyView = findViewById<TextView>(R.id.emptyView)
        this.lv?.emptyView = emptyView

        // Configure onclick event
        this.lv?.isClickable = true
        this.lv?.setOnItemClickListener lambda@{ parent, _, position, _ ->
            if(!checkPermission(permission.CALL_PHONE)) {
                requestPermission((permission.CALL_PHONE))
                return@lambda
            }
            val selectedItem = parent.getItemAtPosition(position) as Map<String, String>
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + selectedItem["number"]))
            startActivity(callIntent)
        }
    }
}
