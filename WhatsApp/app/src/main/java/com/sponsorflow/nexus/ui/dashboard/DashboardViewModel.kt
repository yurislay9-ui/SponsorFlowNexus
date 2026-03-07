package com.sponsorflow.nexus.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val totalMessagesSent: Int = 0,
    val totalMessagesFailed: Int = 0,
    val successRate: Double = 0.0,
    val dailyMessageCount: Int = 0,
    val statusText: String = "Idle",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            try {
                // Load data from repository or use case
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    statusText = "Ready"
                )
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateStats(sent: Int, failed: Int, daily: Int) {
        val successRate = if (sent > 0) (sent - failed).toDouble() / sent.toDouble() else 0.0
        _dashboardState.value = _dashboardState.value.copy(
            totalMessagesSent = sent,
            totalMessagesFailed = failed,
            successRate = successRate,
            dailyMessageCount = daily
        )
    }
}