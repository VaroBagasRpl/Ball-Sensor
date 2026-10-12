package com.example.balanceball.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Controller pembungkus SensorManager dan SensorEventListener.
 * Mengelola pendaftaran listener untuk Accelerometer, Gyroscope, Rotation Vector, dan Light Sensor.
 * Menyediakan pemulusan data (low-pass filter alpha ~0.10 untuk akselerometer/giroskop & circular smoothing untuk kompas).
 * Giroskop didaftarkan secara dinamis hanya jika mode kontrol aktif adalah GYRO.
 */
class SensorController(context: Context) : SensorEventListener {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _sensorState = MutableStateFlow(
        SensorState(
            hasAccelerometer = accelerometer != null,
            hasGyroscope = gyroscope != null,
            hasRotationVector = rotationVector != null,
            hasLightSensor = lightSensor != null,
        ),
    )
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    @Volatile
    private var isRegistered = false

    private var isGyroMode = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var filteredAccelX = 0f
    private var filteredAccelY = 0f
    private var filteredAccelZ = 0f
    private val alphaAccel = 0.10f

    private var filteredGyroX = 0f
    private var filteredGyroY = 0f
    private var filteredGyroZ = 0f
    private val alphaGyro = 0.15f

    private var smoothedSin = 0f
    private var smoothedCos = 1f
    private val alphaCompass = 0.15f

    /**
     * Memperbarui mode kontrol aktif dari UI/ViewModel.
     * Mendaftarkan/mencabut listener Gyroscope secara dinamis sesuai kebutuhan mode.
     */
    fun setGyroActive(enableGyro: Boolean) {
        isGyroMode = enableGyro
        if (isRegistered) {
            if (enableGyro) {
                gyroscope?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
            } else {
                gyroscope?.let {
                    sensorManager.unregisterListener(this, it)
                }
            }
        }
    }

    /**
     * Meregistrasi listener sensor di onResume().
     * Memiliki proteksi isRegistered agar tidak terjadi double-registration.
     */
    fun start() {
        if (isRegistered) return
        isRegistered = true

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        if (isGyroMode) {
            gyroscope?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        rotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Meng-unregister listener sensor di onPause() untuk menghemat baterai.
     */
    fun stop() {
        if (!isRegistered) return
        isRegistered = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                filteredAccelX += alphaAccel * (rawX - filteredAccelX)
                filteredAccelY += alphaAccel * (rawY - filteredAccelY)
                filteredAccelZ += alphaAccel * (rawZ - filteredAccelZ)

                _sensorState.update {
                    it.copy(
                        accelX = filteredAccelX,
                        accelY = filteredAccelY,
                        accelZ = filteredAccelZ,
                    )
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                filteredGyroX += alphaGyro * (rawX - filteredGyroX)
                filteredGyroY += alphaGyro * (rawY - filteredGyroY)
                filteredGyroZ += alphaGyro * (rawZ - filteredGyroZ)

                _sensorState.update {
                    it.copy(
                        gyroX = filteredGyroX,
                        gyroY = filteredGyroY,
                        gyroZ = filteredGyroZ,
                    )
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val rawAzimuthRad = orientationAngles[0]

                val sinVal = sin(rawAzimuthRad.toDouble()).toFloat()
                val cosVal = cos(rawAzimuthRad.toDouble()).toFloat()

                smoothedSin += alphaCompass * (sinVal - smoothedSin)
                smoothedCos += alphaCompass * (cosVal - smoothedCos)

                val smoothedRad = atan2(smoothedSin.toDouble(), smoothedCos.toDouble()).toFloat()
                var smoothedDeg = Math.toDegrees(smoothedRad.toDouble()).toFloat()

                if (smoothedDeg < 0) {
                    smoothedDeg += 360f
                }

                _sensorState.update {
                    it.copy(azimuthDeg = smoothedDeg)
                }
            }

            Sensor.TYPE_LIGHT -> {
                val currentLux = event.values[0]
                _sensorState.update {
                    it.copy(lux = currentLux)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
