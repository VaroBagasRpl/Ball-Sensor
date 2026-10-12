package com.example.balanceball.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Kartu Kompas Digital menggunakan Canvas.
 * - Jarum kompas dan cincin mata angin berputar mengikuti azimuth (0-360 derajat).
 * - Menangani wrapping 359 -> 0 derajat agar perputaran tidak terjadi balik arah mendadak.
 * - Tampilan derajat ("135°") dan penunjuk arah mata angin (N, NE, E, SE, S, SW, W, NW).
 */
@Composable
fun CompassView(
    azimuthDeg: Float,
    hasRotationVector: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardinalDirections = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val directionText = if (hasRotationVector) {
        val index = (((azimuthDeg + 22.5f) % 360) / 45).toInt().coerceIn(0, 7)
        cardinalDirections[index]
    } else {
        "N/A"
    }

    var targetRotation by remember { mutableFloatStateOf(0f) }
    var currentBase by remember { mutableFloatStateOf(0f) }

    if (hasRotationVector) {
        val target = -azimuthDeg
        var diff = (target - currentBase) % 360f
        if (diff < -180f) diff += 360f
        if (diff > 180f) diff -= 360f
        currentBase += diff
        targetRotation = currentBase
    }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "compassRotation",
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "KOMPAS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 8.dp.toPx()

                drawCircle(
                    color = surfaceVariantColor,
                    radius = radius,
                    center = center,
                )

                if (hasRotationVector) {
                    rotate(degrees = animatedRotation, pivot = center) {
                        for (i in 0 until 360 step 15) {
                            val tickRad = Math.toRadians(i.toDouble())
                            val isMajor = i % 90 == 0
                            val tickLength = if (isMajor) 12.dp.toPx() else 6.dp.toPx()

                            val startX = center.x + (radius - tickLength) * sin(tickRad).toFloat()
                            val startY = center.y - (radius - tickLength) * cos(tickRad).toFloat()
                            val endX = center.x + radius * sin(tickRad).toFloat()
                            val endY = center.y - radius * cos(tickRad).toFloat()

                            drawLine(
                                color = if (isMajor) primaryColor else onSurfaceColor.copy(alpha = 0.3f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx(),
                            )
                        }

                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 10.dp.toPx())
                            lineTo(center.x - 7.dp.toPx(), center.y)
                            lineTo(center.x + 7.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(northPath, color = Color(0xFFFF3B30))

                        val southPath = Path().apply {
                            moveTo(center.x, center.y + radius - 10.dp.toPx())
                            lineTo(center.x - 7.dp.toPx(), center.y)
                            lineTo(center.x + 7.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(southPath, color = onSurfaceColor.copy(alpha = 0.4f))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (hasRotationVector) "${azimuthDeg.roundToInt()}°" else "N/A",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = directionText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor,
                )
            }
        }
    }
}
