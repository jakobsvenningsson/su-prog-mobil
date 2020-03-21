package com.example.mprog_project

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CurrencyService(val context: Context) {
    private val apiOpen = "https://api.exchangeratesapi.io"

    suspend fun currencies() = suspendCoroutine<ArrayList<String>> { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "${apiOpen}/latest"
        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                val data = response.getJSONObject("rates")
                var currencies = ArrayList<String>()
                data.keys().forEach { id ->
                    currencies.add(id)
                }
                cont.resume(currencies)
            },
            Response.ErrorListener { error ->
                cont.resumeWithException(error)
            })
        queue.add(req)
    }

    suspend fun convert(from: String, to: String) = suspendCoroutine<Double> { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "${apiOpen}/latest?base=${from}&symbols=${to}"
        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                val data = response.getJSONObject("rates")
                val exchangeRate = data.optDouble(to, 0.0)
                cont.resume(exchangeRate)
            },
            Response.ErrorListener { error ->
                cont.resumeWithException(error)
            }
        )
        queue.add(req)
    }

    suspend fun history(from: String,
                        to: String,
                        start: String,
                        end: String) = suspendCoroutine<ArrayList<Pair<String, Double>>> { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "${apiOpen}/history?base=${from}&symbols=${to}&start_at=${start}&end_at=${end}"
        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                val data = response.getJSONObject("rates")
                var history = ArrayList<Pair<String, Double>>()
                data.keys().forEach { date ->
                    val rate = data.getJSONObject(date).getDouble(to)
                    history.add(Pair(date, rate))
                }
                cont.resume(history)
            },
            Response.ErrorListener { error ->
                cont.resumeWithException(error)
            }
        )
        queue.add(req)
    }
}