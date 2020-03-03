package com.example.a7_3_1

import android.content.Context
import android.net.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var view: TextView? = null
    private var networks: MutableList<Network> = emptyList<Network>().toMutableList()

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            Log.d("networkCallback", "onAvailable ")
            view?.text = "Yes!"
            if(network != null) networks.add(network)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.d("networkCallback", "onUnavailable ")
            view?.text = "Network unavailible!"
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("networkCallback", "onLost ")
            networks.remove(network)
            if(networks.size == 0) view?.text = "No!"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.view = findViewById<TextView>(R.id.isConnectedView).apply {
            text = "No!"
        }
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}
