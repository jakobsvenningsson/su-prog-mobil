package com.example.mprog_project

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DateFormatter(private val start: LocalDate) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val formatter = DateTimeFormatter.ofPattern("MM-dd")
        return formatter.format(start.plusDays(value.toLong()))
    }
}