package com.leticia.moodmirror.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class SensorManagerHelper(
    context: Context,
    private val onLightSensorAvailable: (Boolean) -> Unit,
    private val onLightChanged: (Float) -> Unit,
    private val onMotionChanged: (Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    fun register() {
        // Fallback explicito quando nao existe sensor de luz no dispositivo.
        onLightSensorAvailable(lightSensor != null)

        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                onLightChanged(event.values[0])
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = abs(currentAcceleration - lastAcceleration)
                lastAcceleration = currentAcceleration

                // Limite simples para distinguir estabilidade de movimento.
                onMotionChanged(delta > 1.2f)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
