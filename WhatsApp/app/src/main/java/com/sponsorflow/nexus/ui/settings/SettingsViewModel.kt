package com.sponsorflow.nexus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val minDelayMs: Long = 1000L,
    val maxDelayMs: Long = 5000L,
    val maxDailyMessages: Int = 500,
    val batchSize: Int = 10,
    val enableBanDetection: Boolean = true,
    val enableRiskAssessment: Boolean = true,
    val enableNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)

class SettingsViewModel : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            try {
                // Load from repository
                _settingsState.value = _settingsState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(isLoading = false)
            }
        }
    }

    fun saveSettings(
        minDelayMs: Long,
        maxDelayMs: Long,
        maxDailyMessages: Int,
        batchSize: Int,
        enableBanDetection: Boolean,
        enableRiskAssessment: Boolean,
        enableNotifications: Boolean,
        darkMode: Boolean
    ) {
        viewModelScope.launch {
            try {
                _settingsState.value = _settingsState.value.copy(
                    minDelayMs = minDelayMs,
                    maxDelayMs = maxDelayMs,
                    maxDailyMessages = maxDailyMessages,
                    batchSize = batchSize,
                    enableBanDetection = enableBanDetection,
                    enableRiskAssessment = enableRiskAssessment,
                    enableNotifications = enableNotifications,
                    darkMode = darkMode
                )
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            _settingsState.value = SettingsState()
            _saveResult.value = true
        }
    }
}