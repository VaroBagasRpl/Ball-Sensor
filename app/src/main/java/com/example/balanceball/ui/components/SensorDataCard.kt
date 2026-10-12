package com.example.balanceball.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balanceball.sensor.SensorState
import com.example.balanceball.ui.ControlMode
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SensorDataCard(
    sensorState: SensorState,
    controlMode: ControlMode,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LIVE SENSOR DATA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "MODE: ${controlMode.name}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Accelerometer
        SensorDataRow(
            label = "1. Accelerometer (Motion)",
            value = if (sensorState.hasAccelerometer) {
                String.format(
                    Locale.US,
                    "X: %.2f  Y: %.2f  Z: %.2f m/s²",
                    sensorState.accelX,
                    sensorState.accelY,
                    sensorState.accelZ,
                )
            } else {
                "Sensor tidak tersedia"
            },
            isAvailable = sensorState.hasAccelerometer,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Gyroscope
        SensorDataRow(
            label = "2. Gyroscope (Motion)",
            value = if (sensorState.hasGyroscope) {
                String.format(
                    Locale.US,
                    "X: %.2f  Y: %.2f  Z: %.2f rad/s",
                    sensorState.gyroX,
                    sensorState.gyroY,
                    sensorState.gyroZ,
                )
            } else {
                "Sensor tidak tersedia"
            },
            isAvailable = sensorState.hasGyroscope,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Rotation Vector
        SensorDataRow(
            label = "3. Rotation Vector (Position)",
            value = if (sensorState.hasRotationVector) {
                "Azimuth: ${sensorState.azimuthDeg.roundToInt()}°"
            } else {
                "Sensor tidak tersedia"
            },
            isAvailable = sensorState.hasRotationVector,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Light Sensor
        SensorDataRow(
            label = "4. Light (Environment)",
            value = if (sensorState.hasLightSensor) {
                String.format(Locale.US, "Lux: %.1f lx", sensorState.lux)
            } else {
                "Sensor tidak tersedia"
            },
            isAvailable = sensorState.hasLightSensor,
        )
    }
}

@Composable
private fun SensorDataRow(
    label: String,
    value: String,
    isAvailable: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isAvailable) Color(0xFF00E676) else Color(0xFFFF5252),
                    shape = CircleShape,
                ),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
