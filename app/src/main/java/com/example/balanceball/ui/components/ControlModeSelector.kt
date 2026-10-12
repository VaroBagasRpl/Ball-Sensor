package com.example.balanceball.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.balanceball.ui.ControlMode

@Composable
fun ControlModeSelector(
    currentMode: ControlMode,
    hasGyroscope: Boolean,
    onModeSelected: (ControlMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
        ) {
            ControlModeButton(
                label = "Tilt",
                isSelected = currentMode == ControlMode.TILT,
                isDisabled = false,
                onClick = { onModeSelected(ControlMode.TILT) },
                modifier = Modifier.weight(1f),
            )
            ControlModeButton(
                label = if (hasGyroscope) "Gyro" else "Gyro (N/A)",
                isSelected = (currentMode == ControlMode.GYRO) && hasGyroscope,
                isDisabled = !hasGyroscope,
                onClick = { onModeSelected(ControlMode.GYRO) },
                modifier = Modifier.weight(1f),
            )
            ControlModeButton(
                label = "Sentuh",
                isSelected = currentMode == ControlMode.TOUCH,
                isDisabled = false,
                onClick = { onModeSelected(ControlMode.TOUCH) },
                modifier = Modifier.weight(1f),
            )
        }

        if (!hasGyroscope && (currentMode == ControlMode.GYRO)) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gyroscope tidak tersedia di perangkat ini. Otomatis fallback ke Mode Tilt.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun ControlModeButton(
    label: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = !isDisabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
        )
    }
}
