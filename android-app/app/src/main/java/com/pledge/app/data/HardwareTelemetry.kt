package com.pledge.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.TrafficStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * HardwareTelemetry acts as the sovereign hardware sensor oracle on Android.
 * Reads directly from Android SensorManager (Step Counter / Step Detector)
 * and NetworkStats/TrafficStats without centralized servers.
 */
class HardwareTelemetry(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var initialStepOffset = -1
    private val _realStepsFlow = MutableStateFlow(0)
    val realStepsFlow: StateFlow<Int> = _realStepsFlow.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening || sensorManager == null) return

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } ?: stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialStepOffset < 0) {
                initialStepOffset = totalSteps
            }
            val sessionSteps = (totalSteps - initialStepOffset).coerceAtLeast(0)
            _realStepsFlow.value = sessionSteps
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            _realStepsFlow.value += 1
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun hasHardwareStepSensor(): Boolean {
        return stepCounterSensor != null || stepDetectorSensor != null
    }

    /**
     * Reads actual cumulative network traffic in Megabytes
     */
    fun getNetworkTrafficMB(): Double {
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val totalBytes = (rx + tx).coerceAtLeast(0)
        return totalBytes / (1024.0 * 1024.0)
    }
}
