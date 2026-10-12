package com.example.balanceball.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balanceball.sensor.SensorState
import com.example.balanceball.ui.components.ArenaView
import com.example.balanceball.ui.components.CompassView
import com.example.balanceball.ui.components.ControlModeSelector
import com.example.balanceball.ui.components.LightMeterCard
import com.example.balanceball.ui.components.SensitivityCard
import com.example.balanceball.ui.components.SensorDataCard
import com.example.balanceball.ui.components.ThemeModeSelector
import com.example.balanceball.ui.theme.ThemeMode

@Composable
fun MainScreen(
    sensorState: SensorState,
    uiState: MainUiState,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetControlMode: (ControlMode) -> Unit,
    onSetSensitivity: (Float) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onResetBall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val insets = WindowInsets.systemBars.asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(insets)
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sensor Playground",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Motion, Position & Environment",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }

            ThemeModeSelector(
                currentMode = uiState.themeMode,
                onModeSelected = onSetThemeMode,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        ControlModeSelector(
            currentMode = uiState.controlMode,
            hasGyroscope = sensorState.hasGyroscope,
            onModeSelected = onSetControlMode,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(14.dp))

        ArenaView(
            accelX = sensorState.accelX,
            accelY = sensorState.accelY,
            accelZ = sensorState.accelZ,
            gyroX = sensorState.gyroX,
            gyroY = sensorState.gyroY,
            gyroZ = sensorState.gyroZ,
            azimuthDeg = sensorState.azimuthDeg,
            controlMode = uiState.controlMode,
            sensitivity = uiState.sensitivity,
            hasAccelerometer = sensorState.hasAccelerometer,
            hasGyroscope = sensorState.hasGyroscope,
            soundEnabled = uiState.soundEnabled,
            hapticEnabled = uiState.hapticEnabled,
            resetTrigger = uiState.resetTrigger,
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
        )

        Spacer(modifier = Modifier.height(14.dp))

        SensitivityCard(
            sensitivity = uiState.sensitivity,
            onSensitivityChange = onSetSensitivity,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompassView(
                azimuthDeg = sensorState.azimuthDeg,
                hasRotationVector = sensorState.hasRotationVector,
                modifier = Modifier.weight(1f),
            )

            LightMeterCard(
                lux = sensorState.lux,
                hasLightSensor = sensorState.hasLightSensor,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SensorDataCard(
            sensorState = sensorState,
            controlMode = uiState.controlMode,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilledTonalButton(
                onClick = onResetBall,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = "Reset Bola",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }

            OutlinedIconToggleButton(
                checked = uiState.soundEnabled,
                onCheckedChange = { onToggleSound() },
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    text = if (uiState.soundEnabled) "🔊" else "🔇",
                    fontSize = 16.sp,
                )
            }

            OutlinedIconToggleButton(
                checked = uiState.hapticEnabled,
                onCheckedChange = { onToggleHaptic() },
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    text = if (uiState.hapticEnabled) "📳" else "📴",
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
