package com.example.balanceball.ui

import androidx.lifecycle.ViewModel
import com.example.balanceball.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ControlMode {
    TILT,
    GYRO,
    TOUCH,
}

data class MainUiState(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val controlMode: ControlMode = ControlMode.TILT,
    val sensitivity: Float = 0.8f,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val resetTrigger: Int = 0,
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setControlMode(mode: ControlMode) {
        _uiState.update { it.copy(controlMode = mode) }
    }

    fun setSensitivity(value: Float) {
        _uiState.update { it.copy(sensitivity = value.coerceIn(0.2f, 2.0f)) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun resetBall() {
        _uiState.update { it.copy(resetTrigger = it.resetTrigger + 1) }
    }
}
