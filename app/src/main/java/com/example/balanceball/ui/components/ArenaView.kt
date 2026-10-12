package com.example.balanceball.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balanceball.ui.ControlMode
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

private data class TrailPoint(val x: Float, val y: Float, val alpha: Float)
private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var alpha: Float,
)

/**
 * Arena interaktif dengan simulasi fisika 60fps.
 * - Mendukung 3 Mode Kontrol: TILT (Akselerometer), GYRO (Giroskop), & TOUCH (Sentuh/Lempar).
 * - Sensitivitas yang dapat disesuaikan, dead zone, low-pass filter, damping 0.985f, dan pantulan 0.45f.
 * - Fling physics saat bola dilepas pada mode sentuh.
 * - Shake Detection (> 2.7g) mereset bola ke tengah & memicu efek partikel confetti.
 */
@Composable
fun ArenaView(
    accelX: Float,
    accelY: Float,
    accelZ: Float,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    azimuthDeg: Float,
    controlMode: ControlMode,
    sensitivity: Float,
    hasAccelerometer: Boolean,
    hasGyroscope: Boolean,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    resetTrigger: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val ballSizeDp = 42.dp
    val ballRadiusPx = with(density) { (ballSizeDp / 2).toPx() }
    val maxSpeedPx = with(density) { 900.dp.toPx() }

    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var velX by remember { mutableFloatStateOf(0f) }
    var velY by remember { mutableFloatStateOf(0f) }

    var isTouchingBall by remember { mutableStateOf(false) }

    var lastImpactTime by remember { mutableLongStateOf(0L) }
    var lastShakeTime by remember { mutableLongStateOf(0L) }

    val trailList = remember { mutableStateListOf<TrailPoint>() }
    val confettiList = remember { mutableStateListOf<ConfettiParticle>() }
    val velocityTracker = remember { VelocityTracker() }

    val accentColor = remember(azimuthDeg) {
        Color.hsv(azimuthDeg.coerceIn(0f, 360f), 0.85f, 0.95f)
    }

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 40)
        } catch (_: Exception) {
            null
        }
    }

    val vibrator = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    val triggerImpact: () -> Unit = {
        val now = System.currentTimeMillis()
        if ((now - lastImpactTime) >= 120) {
            lastImpactTime = now

            if (soundEnabled) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                } catch (_: Exception) {}
            }

            if (hapticEnabled) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(25)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val triggerShakeEffect: (centerX: Float, centerY: Float) -> Unit = { cx, cy ->
        confettiList.clear()
        repeat(30) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            confettiList.add(
                ConfettiParticle(
                    x = cx,
                    y = cy,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    color = Color.hsv(Random.nextFloat() * 360f, 0.9f, 1.0f),
                    size = Random.nextFloat() * 12f + 6f,
                    alpha = 1.0f,
                ),
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = accentColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
            ),
    ) {
        val widthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val heightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx, resetTrigger) {
            if (widthPx > 0 && heightPx > 0) {
                ballX = widthPx / 2f
                ballY = heightPx / 2f
                velX = 0f
                velY = 0f
                trailList.clear()
            }
        }

        LaunchedEffect(controlMode) {
            velX = 0f
            velY = 0f
        }

        LaunchedEffect(widthPx, heightPx, controlMode, sensitivity, accelX, accelY, gyroX, gyroY) {
            if (widthPx <= 0 || heightPx <= 0) return@LaunchedEffect

            var lastFrameNanos = System.nanoTime()

            while (true) {
                withFrameNanos { frameNanos ->
                    val dtNanos = frameNanos - lastFrameNanos
                    lastFrameNanos = frameNanos
                    val dt = (dtNanos / 1_000_000_000f).coerceIn(0.001f, 0.032f)

                    if (hasAccelerometer) {
                        val totalAccelG = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ) / 9.81f
                        val currentTime = System.currentTimeMillis()
                        if (totalAccelG > 2.7f && (currentTime - lastShakeTime) > 1000) {
                            lastShakeTime = currentTime
                            ballX = widthPx / 2f
                            ballY = heightPx / 2f
                            velX = 0f
                            velY = 0f
                            triggerImpact()
                            triggerShakeEffect(ballX, ballY)
                        }
                    }

                    if (!isTouchingBall) {
                        val activeMode = if (controlMode == ControlMode.GYRO && !hasGyroscope) {
                            ControlMode.TILT
                        } else {
                            controlMode
                        }

                        when (activeMode) {
                            ControlMode.TILT -> {
                                if (hasAccelerometer) {
                                    val axRaw = if (abs(accelX) > 0.4f) -accelX else 0f
                                    val ayRaw = if (abs(accelY) > 0.4f) -accelY else 0f

                                    val baseGain = 560f * sensitivity
                                    velX += axRaw * baseGain * dt
                                    velY += ayRaw * baseGain * dt
                                }
                            }

                            ControlMode.GYRO -> {
                                if (hasGyroscope) {
                                    val gxRaw = if (abs(gyroY) > 0.05f) gyroY else 0f
                                    val gyRaw = if (abs(gyroX) > 0.05f) gyroX else 0f

                                    val gyroGain = 480f * sensitivity
                                    velX += gxRaw * gyroGain * dt
                                    velY += gyRaw * gyroGain * dt
                                }
                            }

                            ControlMode.TOUCH -> {
                            }
                        }
                    }

                    velX *= 0.985f
                    velY *= 0.985f

                    val speed = sqrt(velX * velX + velY * velY)
                    if (speed > maxSpeedPx) {
                        velX = (velX / speed) * maxSpeedPx
                        velY = (velY / speed) * maxSpeedPx
                    }

                    var nextX = ballX + velX * dt * 60f
                    var nextY = ballY + velY * dt * 60f

                    val maxX = widthPx - ballRadiusPx
                    val maxY = heightPx - ballRadiusPx

                    var collided = false

                    if (nextX < ballRadiusPx) {
                        nextX = ballRadiusPx
                        velX = -velX * 0.45f
                        collided = true
                    } else if (nextX > maxX) {
                        nextX = maxX
                        velX = -velX * 0.45f
                        collided = true
                    }

                    if (nextY < ballRadiusPx) {
                        nextY = ballRadiusPx
                        velY = -velY * 0.45f
                        collided = true
                    } else if (nextY > maxY) {
                        nextY = maxY
                        velY = -velY * 0.45f
                        collided = true
                    }

                    if (collided) {
                        triggerImpact()
                    }

                    ballX = nextX
                    ballY = nextY

                    trailList.add(0, TrailPoint(ballX, ballY, 1.0f))
                    if (trailList.size > 25) {
                        trailList.removeAt(trailList.size - 1)
                    }

                    val iterator = confettiList.iterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        p.x += p.vx
                        p.y += p.vy
                        p.alpha -= 0.02f
                        if (p.alpha <= 0f) {
                            iterator.remove()
                        }
                    }
                }
            }
        }

        val dragModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    val dist = sqrt((offset.x - ballX) * (offset.x - ballX) + (offset.y - ballY) * (offset.y - ballY))
                    if (controlMode == ControlMode.TOUCH || dist <= ballRadiusPx * 2.5f) {
                        isTouchingBall = true
                        velX = 0f
                        velY = 0f
                        velocityTracker.resetTracking()
                    }
                },
                onDragEnd = {
                    if (isTouchingBall) {
                        val velocity = velocityTracker.calculateVelocity()
                        velX = (velocity.x * 0.6f).coerceIn(-maxSpeedPx, maxSpeedPx)
                        velY = (velocity.y * 0.6f).coerceIn(-maxSpeedPx, maxSpeedPx)
                        isTouchingBall = false
                    }
                },
                onDragCancel = {
                    isTouchingBall = false
                },
                onDrag = { change, dragAmount ->
                    if (isTouchingBall) {
                        change.consume()
                        ballX = (ballX + dragAmount.x).coerceIn(ballRadiusPx, widthPx - ballRadiusPx)
                        ballY = (ballY + dragAmount.y).coerceIn(ballRadiusPx, heightPx - ballRadiusPx)

                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                        trailList.add(0, TrailPoint(ballX, ballY, 1.0f))
                        if (trailList.size > 25) {
                            trailList.removeAt(trailList.size - 1)
                        }
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(dragModifier),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                var x = gridSpacing
                while (x < size.width) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                        pathEffect = gridPathEffect,
                    )
                    x += gridSpacing
                }

                var y = gridSpacing
                while (y < size.height) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = gridPathEffect,
                    )
                    y += gridSpacing
                }

                trailList.forEachIndexed { index, point ->
                    val fadeAlpha = ((25 - index) / 25f) * 0.4f
                    val radius = ballRadiusPx * ((25 - index) / 25f)
                    drawCircle(
                        color = accentColor.copy(alpha = fadeAlpha),
                        radius = radius,
                        center = Offset(point.x, point.y),
                    )
                }

                val currentSpeed = sqrt(velX * velX + velY * velY)
                val glowRadius = ballRadiusPx + (currentSpeed * 1.8f).coerceAtMost(30.dp.toPx())
                val glowAlpha = (0.25f + (currentSpeed / 20f)).coerceAtMost(0.7f)

                drawCircle(
                    color = accentColor.copy(alpha = glowAlpha * 0.4f),
                    radius = glowRadius * 1.4f,
                    center = Offset(ballX, ballY),
                )

                drawCircle(
                    color = accentColor,
                    radius = ballRadiusPx,
                    center = Offset(ballX, ballY),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = ballRadiusPx * 0.4f,
                    center = Offset(ballX - ballRadiusPx * 0.25f, ballY - ballRadiusPx * 0.25f),
                )

                confettiList.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        radius = p.size,
                        center = Offset(p.x, p.y),
                    )
                }
            }

            if (!hasAccelerometer && controlMode == ControlMode.TILT) {
                Text(
                    text = "Akselerometer tidak tersedia\nGeser jari untuk menggerakkan bola",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
