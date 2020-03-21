package com.example.mprog_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


enum class CURRENCY_MSG_TYPE {
    FROM, TO, UPDATE_AMOUNT, UPDATE_HISTORY
}

class MainActivity : AppCompatActivity() {
    val mostRecentReqCode = 100
    val favReqCode = 101

    var inputFrom: EditText? = null
    var inputTo: EditText? = null
    var spinnerFrom: Spinner? = null
    var spinnerTo: Spinner? = null
    var currencies: ArrayList<String>? = null
    var api: CurrencyService? = null
    var lineChart: LineChart? = null
    var mostRecentTextView: TextView? = null
    var favouritesTextView: TextView? = null
    var addToFavouritesTextView: TextView? = null

    var mostRecent = mutableListOf<Conversion>()
    var favouriteList = mutableListOf<Conversion>()
    var favouriteMap = mutableSetOf<Conversion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.api = CurrencyService(this)

        // This channel is used to communicate changes in the two currencies
        val currencyCh = Channel<Pair<CURRENCY_MSG_TYPE, Int>>()

        initLineChart()
        initInputFields(currencyCh)
        initSpinners(currencyCh)
        initHistoryButtons(currencyCh)
        initUIUpdateCoroutine(currencyCh)
        initFavourites()
        initMostRecent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }

        var s: String? = null
        when(requestCode) {
            favReqCode -> {
                s = R.string.FAVOURITES_RETURN_EXTRA.toString()
            }
            mostRecentReqCode -> {
                s = R.string.MOST_RECENT_RETURN_EXTRA.toString()
            }
        }

        val tmp: ArrayList<Conversion> = data?.getParcelableArrayListExtra<Conversion>(s!!) ?: arrayListOf()
        if(tmp.isEmpty()) {
            return
        }
        val conversion = tmp[0]
        this.spinnerTo?.setSelection(conversion.toIndex)
        this.spinnerFrom?.setSelection(conversion.fromIndex)
    }

    // UI update methods

    private fun updateRates(from: String, to: String) {
        val amount = this.inputFrom?.text.toString()
        if(amount.isEmpty()) {
            this@MainActivity.inputTo?.text?.clear()
            return
        }
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) {
            api?.convert(from, to)?.let { r ->
                this@MainActivity.inputTo?.setText((amount.toDouble() * r).toString())
            }
        }
    }

    private fun updateChart(from: String, to: String, period: Long = 7) {
        val amount = this.inputFrom?.text.toString()
        if(amount.isEmpty()) return

        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) lambda@{
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.now().minusDays(period)
            val end = LocalDate.now()
            val history= api?.history(
                from,
                to,
                start.format(formatter),
                end.format(formatter)
            )
            history?.sortBy { it.first }
            if(history == null) return@lambda
            val axes = history.map { e ->
                val days = ChronoUnit.DAYS.between(start, LocalDate.parse(e.first))
                Entry(days.toFloat(), e.second.toFloat() * amount.toFloat())
            }

            lineChart?.xAxis?.valueFormatter = DateFormatter(start)
            lineChart?.xAxis?.granularity = 1f
            lineChart?.xAxis?.axisMinimum = axes.first().x
            lineChart?.xAxis?.axisMaximum = axes.last().x
            lineChart?.xAxis?.setLabelCount(axes.size, false)

            val dataSet = LineDataSet(axes, "label").apply {
                setDrawFilled(true)
            }
            lineChart?.data = LineData(dataSet)
            lineChart?.invalidate()
        }
    }

    // UI Setup methods

    private fun initSpinners(ch: Channel<Pair<CURRENCY_MSG_TYPE, Int>>) {
        this.spinnerFrom = findViewById(R.id.spinnerBase)
        this.spinnerFrom?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                CoroutineScope(Dispatchers.Default).launch {
                    ch.send(Pair(CURRENCY_MSG_TYPE.FROM, position))
                }
            }
        }
        this.spinnerTo = findViewById(R.id.spinnerTo)
        this.spinnerTo?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                CoroutineScope(Dispatchers.Default).launch {
                    ch.send(Pair(CURRENCY_MSG_TYPE.TO, position))
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) lambda@{
            val currencies = api?.currencies()
            if(currencies == null) return@lambda
            val adapter = ArrayAdapter<String>(applicationContext, android.R.layout.simple_spinner_dropdown_item, currencies)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFrom?.adapter = adapter
            spinnerTo?.adapter = adapter
            if(mostRecent.isEmpty()) {
                spinnerTo?.setSelection(1)
            } else {
                spinnerTo?.setSelection(mostRecent.first().toIndex)
                spinnerFrom?.setSelection(mostRecent.first().fromIndex)
            }
            this@MainActivity.currencies = currencies
        }
    }

    private fun initInputFields(ch: Channel<Pair<CURRENCY_MSG_TYPE, Int>>) {
        // Configure base currency input
        this.inputFrom = findViewById<EditText>(R.id.inputFrom).apply {
            setText("1.0")
            addTextChangedListener {
                CoroutineScope(Dispatchers.Default).launch {
                    ch.send(Pair(CURRENCY_MSG_TYPE.UPDATE_AMOUNT, 0))
                }
            }
        }
        // Configure target currency input
        this.inputTo = findViewById<EditText>(R.id.inputTo).apply {
            isEnabled = false
        }
    }

    private fun initUIUpdateCoroutine(ch: Channel<Pair<CURRENCY_MSG_TYPE, Int>>) {
        CoroutineScope(Dispatchers.Default).launch {
            var from: String? = null
            var fromIndex: Int? = null
            var to: String? = null
            var toIndex: Int? = null
            var period = 7L
            for(c in ch) {
                when(c.first) {
                    CURRENCY_MSG_TYPE.FROM -> {
                        fromIndex = c.second
                        from = currencies?.get(c.second)
                    }
                    CURRENCY_MSG_TYPE.TO -> {
                        toIndex = c.second
                        to = currencies?.get(c.second)
                    }
                    CURRENCY_MSG_TYPE.UPDATE_AMOUNT -> Unit
                    CURRENCY_MSG_TYPE.UPDATE_HISTORY -> period = c.second.toLong()
                }
                if(from == null || to == null || fromIndex == null || toIndex == null) continue
                updateRates(from, to)
                updateChart(from, to, period)

                // Update most recent history
                mostRecent.add(0, Conversion(from, to, fromIndex, toIndex))
                mostRecent = mostRecent.takeLast(10).distinct().toMutableList()
                writeStringToPrefs(Gson().toJson(mostRecent), R.string.MOST_RECENT_LIST_KEY.toString())
            }
        }
    }

    private fun initHistoryButtons(ch: Channel<Pair<CURRENCY_MSG_TYPE, Int>>) {
        val btn1W = findViewById<Button>(R.id.historyBtn1W)
        val btn2W = findViewById<Button>(R.id.historyBtn2W)
        val btn1M = findViewById<Button>(R.id.historyBtn1M)

        btn1W.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch lambda@{
                ch.send(Pair(CURRENCY_MSG_TYPE.UPDATE_HISTORY, 7))
            }
        }
        btn2W.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch lambda@{
                ch.send(Pair(CURRENCY_MSG_TYPE.UPDATE_HISTORY, 14))
            }
        }
        btn1M.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch lambda@{
                ch.send(Pair(CURRENCY_MSG_TYPE.UPDATE_HISTORY, 30))
            }
        }
    }

    private fun initMostRecent() {
        this.mostRecentTextView = findViewById<TextView>(R.id.mostRecentTextView)
        this.mostRecentTextView?.setOnClickListener {
            val intent = Intent(this, MostRecentActivity::class.java).apply {
                putExtra(R.string.MOST_RECENT_EXTRA.toString(), ArrayList(mostRecent));
            }
            startActivityForResult(intent, mostRecentReqCode)
        }
        // Read old values from shared prefs.
        val json = readStringFromPrefs(R.string.MOST_RECENT_LIST_KEY.toString())
        if(json != null) {
            this.mostRecent = Serializer.fromJson<MutableList<Conversion>>(json)
        }
    }

    private fun initFavourites() {
        this.favouritesTextView = findViewById<TextView>(R.id.favouritesTextView)
        this.addToFavouritesTextView = findViewById<TextView>(R.id.addToFavouritesTextView)
        this.addToFavouritesTextView?.setOnClickListener {
            val from = spinnerFrom?.selectedItem as String
            val fromIndex = spinnerFrom?.selectedItemPosition ?: 0
            val to = spinnerTo?.selectedItem as String
            val toIndex = spinnerTo?.selectedItemPosition ?: 0
            val conversion = Conversion(from, to, fromIndex, toIndex)

            if(!favouriteMap.contains(conversion)) {
                favouriteList.add(conversion)
                favouriteMap.add(conversion)
                writeStringToPrefs(Gson().toJson(favouriteList), R.string.FAV_LIST_KEY.toString())
                writeStringToPrefs(Gson().toJson(favouriteMap), R.string.FAV_MAP_KEY.toString())
            }
        }

        this.favouritesTextView?.setOnClickListener {
            val intent = Intent(this, FavouritesActivity::class.java).apply {
                putExtra(R.string.FAVOURITES_EXTRA.toString(), ArrayList(favouriteList));
            }
            startActivityForResult (intent, favReqCode)
        }

        val jsonList = readStringFromPrefs(R.string.FAV_LIST_KEY.toString())
        if(jsonList != null) {
            this.favouriteList = Serializer.fromJson<MutableList<Conversion>>(jsonList)
        }
        val jsonMap = readStringFromPrefs(R.string.FAV_MAP_KEY.toString())
        if(jsonMap != null) {
            this.favouriteMap = Serializer.fromJson<MutableSet<Conversion>>(jsonMap)
        }
    }

    private fun initLineChart() {
        this.lineChart = findViewById<LineChart>(R.id.lineChart).apply {
            legend.isEnabled = false
            description.isEnabled = false
        }
    }

    // Helpers

    private fun writeStringToPrefs(s: String, key: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(key, s)
            commit()
        }
    }

    private fun readStringFromPrefs(key: String): String? {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getString(key, null)
    }
}
