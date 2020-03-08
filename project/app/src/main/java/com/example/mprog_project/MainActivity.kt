package com.example.mprog_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.collections.ArrayList

enum class CURRENCY_MSG_TYPE {
    FROM, TO, UPDATE_AMOUNT, UPDATE_HISTORY
}

class MainActivity : AppCompatActivity() {
    var inputFrom: EditText? = null
    var inputTo: EditText? = null
    var spinnerFrom: Spinner? = null
    var spinnerTo: Spinner? = null
    var currencies: ArrayList<String>? = null
    var api: CurrencyService? = null
    var lineChart: LineChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.api = CurrencyService(this)

        //  Configure LineChartView
        this.lineChart = findViewById<LineChart>(R.id.lineChart).apply {
            legend.isEnabled = false
            description.isEnabled = false
        }

        // This channel is used to communicate changes in the two currencies
        val currencyCh = Channel<Pair<CURRENCY_MSG_TYPE, Int>>()

        initInputFields(currencyCh)
        initSpinners(currencyCh)
        initHistoryButtons(currencyCh)
        initUIUpdateCoroutine(currencyCh)
    }

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
            spinnerTo?.setSelection(1)
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
            var to: String? = null
            var period = 7L
            for(c in ch) {
                when(c.first) {
                    CURRENCY_MSG_TYPE.FROM -> from = currencies?.get(c.second)
                    CURRENCY_MSG_TYPE.TO -> to = currencies?.get(c.second)
                    CURRENCY_MSG_TYPE.UPDATE_AMOUNT -> Unit
                    CURRENCY_MSG_TYPE.UPDATE_HISTORY -> period = c.second.toLong()
                }
                if(from == null || to == null) continue
                updateRates(from, to)
                updateChart(from, to, period)
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
}
