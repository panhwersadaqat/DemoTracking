package com.example.demotracking

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var stepCount = 0
    private var previousY = 0f
    private lateinit var stepCountTextView: TextView
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
        notificationIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE)

        // Schedule the first notification
        val initialDelay = 2000L // 2 seconds
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + initialDelay,
            2000L, // 2 seconds
            notificationIntent
        )

        // Initialize the TextView
        stepCountTextView = findViewById(R.id.stepCountTextView)

        //obtain an instance of the accelerometer sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onResume() {
        super.onResume()
        // Register the sensor listener
        sensorManager.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

    }

    override fun onPause() {
        super.onPause()
        // Unregister the sensor listener
        sensorManager.unregisterListener(this)

    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val currentY = event.values[1]
            if (isStep(previousY, currentY)) {
                stepCount++
                stepCountTextView.text = "Step Count: $stepCount"
            }
            previousY = currentY
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun isStep(previousY: Float, currentY: Float): Boolean {
        val threshold = 10f // Adjust the threshold as per your requirement
        return previousY < 0 && currentY >= threshold ||
                previousY >= threshold && currentY < 0
    }
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the notifications when the activity is destroyed
        alarmManager.cancel(notificationIntent)
    }

}