package com.example.mprog_project

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class MostRecentActivity : AppCompatActivity() {
    private var lv: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_most_recent)

        this.lv = findViewById<ListView>(R.id.listView)
        val emptyView = findViewById<TextView>(R.id.emptyElement)
        this.lv?.emptyView = emptyView

        val history = intent.getParcelableArrayListExtra<Conversion>(R.string.MOST_RECENT_EXTRA.toString())
        val adapter = ArrayAdapter<Conversion>(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            history
        )
        this.lv?.adapter = adapter

        // Configure onclick event
        this.lv?.isClickable = true
        this.lv?.setOnItemClickListener lambda@{ parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as Conversion
            println(selectedItem.toString())
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putParcelableArrayListExtra( R.string.MOST_RECENT_RETURN_EXTRA.toString(), ArrayList(listOf(selectedItem)))
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}
