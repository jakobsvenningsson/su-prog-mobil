package com.example.mprog_project

import android.util.Log
import org.json.JSONObject

data class Currency(val code: String,
                    val name: String,
                    val symbol: String,
                    val country: String,
                    val countryCode: String) {
    companion object {
        fun fromJSON(json: JSONObject): Currency {
            val code = json.getString("currency_code")
            val name = json.getString("name")
            val symbol = json.optString("symbol", "")
            val countryCode = json.optString("country_code", "")
            val country = json.getString("country_name")
            return Currency(code, name, symbol, country, countryCode)
        }
    }

    override fun toString(): String {
        return this.code
    }
}
