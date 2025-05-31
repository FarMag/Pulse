package com.example.vkr_watch

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class StepSyncService : Service() {

    companion object {
        var isRunning = false
        private var fakeSteps = 0
        private val handler = Handler(Looper.getMainLooper())
        private val stepRunnable = object : Runnable {
            override fun run() {
                fakeSteps += (5..15).random()
                instance?.sendStepsToPhone(fakeSteps)
                handler.postDelayed(this, 5000)
            }
        }

        private var instance: StepSyncService? = null

        fun startSending() {
            if (!isRunning) {
                isRunning = true
                handler.postDelayed(stepRunnable, 1000)
                Log.d("StepService", "Передача шагов возобновлена")
            }
        }

        fun stopSending() {
            if (isRunning) {
                isRunning = false
                handler.removeCallbacks(stepRunnable)
                Log.d("StepService", "Передача шагов остановлена")
            }
        }
    }

    private lateinit var dataClient: DataClient

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataClient = Wearable.getDataClient(this)
        startForegroundServiceWithNotification()
        startSending() // автостарт при запуске сервиса
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSending()
        instance = null
    }

    private fun sendStepsToPhone(steps: Int) {
        val dataMapRequest = PutDataMapRequest.create("/steps")
        dataMapRequest.dataMap.putInt("steps", steps)
        dataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())
        val request = dataMapRequest.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Log.d("StepService", "Fake steps sent: $steps")
            }
            .addOnFailureListener {
                Log.e("StepService", "Failed to send fake steps", it)
            }
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "step_sync_channel"
        val channelName = "Step Sync"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Отслеживание шагов")
            .setContentText("Данные о шагах передаются на телефон")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}