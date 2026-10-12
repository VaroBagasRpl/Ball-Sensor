package com.example.balanceball.sensor

/**
 * Data class penampung status dan nilai pembacaan dari 4 sensor:
 * 1. Accelerometer (Motion) - accelX, accelY, accelZ
 * 2. Gyroscope (Motion) - gyroX, gyroY, gyroZ (rad/s)
 * 3. Rotation Vector (Position) - azimuthDeg (derajat kompas 0..360)
 * 4. Light (Environment) - lux (intensitas cahaya)
 * Serta flag ketersediaan masing-masing sensor pada perangkat.
 */
data class SensorState(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val azimuthDeg: Float = 0f,
    val lux: Float = 200f,
    val hasAccelerometer: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasRotationVector: Boolean = false,
    val hasLightSensor: Boolean = false,
)
