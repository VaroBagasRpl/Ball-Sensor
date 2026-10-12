package com.example.balanceball

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.balanceball.sensor.SensorController
import com.example.balanceball.ui.ControlMode
import com.example.balanceball.ui.MainScreen
import com.example.balanceball.ui.MainViewModel
import com.example.balanceball.ui.theme.SensorPlaygroundTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorController: SensorController
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorController = SensorController(this)

        setContent {
            val sensorState by sensorController.sensorState.collectAsStateWithLifecycle()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.controlMode) {
                sensorController.setGyroActive(uiState.controlMode == ControlMode.GYRO)
            }

            SensorPlaygroundTheme(
                themeMode = uiState.themeMode,
                currentLux = sensorState.lux,
            ) {
                MainScreen(
                    sensorState = sensorState,
                    uiState = uiState,
                    onSetThemeMode = viewModel::setThemeMode,
                    onSetControlMode = viewModel::setControlMode,
                    onSetSensitivity = viewModel::setSensitivity,
                    onToggleSound = viewModel::toggleSound,
                    onToggleHaptic = viewModel::toggleHaptic,
                    onResetBall = viewModel::resetBall,
                )
            }
        }
    }

    // =========================================================================
    // LIFECYCLE SENSOR MANAGEMENT
    // =========================================================================

    /**
     * LIFECYCLE: onResume
     * Mendaftarkan (register) listener semua sensor ketika aktivitas aktif.
     */
    override fun onResume() {
        super.onResume()
        sensorController.start()
    }

    /**
     * LIFECYCLE: onPause
     * Menghentikan (unregister) listener semua sensor ketika aktivitas dijeda.
     */
    override fun onPause() {
        super.onPause()
        sensorController.stop()
    }
}
