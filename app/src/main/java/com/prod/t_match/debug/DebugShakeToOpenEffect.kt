package com.prod.t_match.debug

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

private const val ShakeThresholdGravity = 2.7f
private const val ShakeCooldownMillis = 1_200L

@Composable
fun DebugShakeToOpenEffect(
	onShake: () -> Unit
) {
	val context = LocalContext.current
	val latestOnShake = rememberUpdatedState(onShake)

	DisposableEffect(context) {
		val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
		val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

		if (sensorManager == null || accelerometer == null) {
			onDispose { }
		} else {
			var lastTriggerAt = 0L
			val listener = object : SensorEventListener {
				override fun onSensorChanged(event: SensorEvent) {
					val x = event.values.getOrNull(0) ?: return
					val y = event.values.getOrNull(1) ?: return
					val z = event.values.getOrNull(2) ?: return
					val gX = x / SensorManager.GRAVITY_EARTH
					val gY = y / SensorManager.GRAVITY_EARTH
					val gZ = z / SensorManager.GRAVITY_EARTH
					val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
					val now = System.currentTimeMillis()

					if (gForce >= ShakeThresholdGravity && now - lastTriggerAt >= ShakeCooldownMillis) {
						lastTriggerAt = now
						latestOnShake.value()
					}
				}

				override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
			}

			sensorManager.registerListener(
				listener,
				accelerometer,
				SensorManager.SENSOR_DELAY_UI
			)

			onDispose {
				sensorManager.unregisterListener(listener)
			}
		}
	}
}
