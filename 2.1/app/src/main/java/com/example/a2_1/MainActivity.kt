package com.example.a2_1

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.Long.Companion.MAX_VALUE

class MainActivity : AppCompatActivity() {
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        job = run(prefs)
        findViewById<Button>(R.id.reset_btn).setOnClickListener {
            job?.cancel()
            prefs.edit().remove(getString(R.string.prime_key)).apply()
            findViewById<TextView>(R.id.max_prime_view).apply { text = "" }
            job = run(prefs)
        }
    }

    private fun run(prefs: SharedPreferences): Job {
        val oldMaxPrime = prefs.getLong(getString(R.string.prime_key), 2L)
        val (job, ch) = numbersFrom(oldMaxPrime)
        val primes = filterPrimes(displayNumbers(ch))
        CoroutineScope(Dispatchers.Main).launch {
            for(prime in primes) {
                Log.d("main", prime.toString())
                prefs.edit().putLong(getString(R.string.prime_key), prime).apply()
                findViewById<TextView>(R.id.max_prime_view).apply { text = prime.toString() }
            }
            Log.d("main", "done.")
        }
        return job
    }

    private fun numbersFrom(start: Long): Pair<Job, ReceiveChannel<Long>> {
        val ch = Channel<Long>()
        val job = CoroutineScope(Dispatchers.Default).launch {
            try {
                for(n in start..MAX_VALUE) {
                    delay(100L)
                    ch.send(n)
                }
            } finally {
                Log.d("numbersFrom", "done.")
                ch.close()
            }
        }
        return Pair(job, ch)
    }

    private fun displayNumbers(numbers: ReceiveChannel<Long>): ReceiveChannel<Long> {
        val ch = Channel<Long>()
        CoroutineScope(Dispatchers.Main).launch {
            for (n in numbers) {
                findViewById<TextView>(R.id.current_number_view).apply { text = n.toString() }
                ch.send(n)
            }
            ch.close()
            Log.d("displayNumbers","done.")
        }
        return ch
    }

    private fun filterPrimes(numbers: ReceiveChannel<Long>): ReceiveChannel<Long> {
        val ch = Channel<Long>()
        CoroutineScope(Dispatchers.Default).launch {
            for (n in numbers) {
                if (isPrime(n)) {
                    ch.send(n)
                }
            }
            ch.close()
            Log.d("filterPrimes","done.")
        }
        return ch
    }

    private fun isPrime(x: Long): Boolean {
        val sqrt = sqrt(x.toDouble()).roundToLong()
        for(i: Long in 2..sqrt) {
            if (x % i == 0L) return false
        }
        return true
    }
}
