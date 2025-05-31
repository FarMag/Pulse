package com.example.vkr_watch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/request_steps") {
            Log.d("Wear", "Получена команда на передачу шагов от телефона")
            isSending = true
            handler.post(stepSender)
            runOnUiThread {
                serviceStatusTextView.text = "Передача активна"
                toggleBtn.text = "Остановить"
            }
        }
    }
    private lateinit var serviceStatusTextView: TextView
    private lateinit var stepsCountTextView: TextView
    private lateinit var toggleBtn: Button

    private lateinit var dataClient: DataClient
    private var fakeSteps = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isSending = true

    private val stepSender = object : Runnable {
        override fun run() {
            if (isSending) {
                fakeSteps += (5..15).random()
                stepsCountTextView.text = "Шаги: $fakeSteps"
                sendStepsToPhone(fakeSteps)
                handler.postDelayed(this, 5000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceStatusTextView = findViewById(R.id.serviceStatusTextView)
        stepsCountTextView = findViewById(R.id.stepsCountTextView)
        toggleBtn = findViewById(R.id.stopServiceBtn)
        dataClient = Wearable.getDataClient(this)

        stepsCountTextView.text = "Шаги: $fakeSteps"
        serviceStatusTextView.text = "Передача активна"
        toggleBtn.text = "Остановить"

        handler.post(stepSender)

        toggleBtn.setOnClickListener {
            isSending = !isSending
            if (isSending) {
                serviceStatusTextView.text = "Передача активна"
                toggleBtn.text = "Остановить"
                handler.post(stepSender)
            } else {
                serviceStatusTextView.text = "Передача остановлена"
                toggleBtn.text = "Продолжить"
                handler.removeCallbacks(stepSender)
            }
        }
    }

    private fun sendStepsToPhone(steps: Int) {
        val dataMapRequest = PutDataMapRequest.create("/steps")
        dataMapRequest.dataMap.putInt("steps", steps)
        dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())
        val request = dataMapRequest.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Log.d("Wear", "Fake steps sent: $steps")
            }
            .addOnFailureListener {
                Log.e("Wear", "Failed to send fake steps", it)
            }
    }

}