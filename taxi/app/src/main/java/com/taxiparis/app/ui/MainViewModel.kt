package com.taxiparis.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiparis.app.data.local.TaxiPrefs
import com.taxiparis.app.domain.model.TaxiState
import com.taxiparis.app.domain.model.LineStatus
import com.taxiparis.app.domain.usecase.SyncDataUseCase
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed class TrafficState {
    object Loading : TrafficState()
    data class Success(val lines: List<LineStatus>) : TrafficState()
    data class Error(val message: String) : TrafficState()
}

data class TaxiUiState(
    val taxiState: TaxiState = TaxiState(0, 0, 0L, false),
    val trafficState: TrafficState = TrafficState.Loading,
    val isRefreshing: Boolean = false,
    val isLoaded: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: TaxiPrefs,
    private val syncData: SyncDataUseCase
) : ViewModel() {

    private val _trafficState = MutableStateFlow<TrafficState>(TrafficState.Loading)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<TaxiUiState> = combine(
        prefs.taxiState,
        _trafficState,
        _isRefreshing
    ) { taxi, traffic, refreshing ->
        TaxiUiState(taxi, traffic, refreshing, isLoaded = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaxiUiState()
    )

    init {
        com.taxiparis.app.worker.UpdateWorker.startImmediately(prefs.context)
    }

    fun updateWidgets() {
        viewModelScope.launch {
            com.taxiparis.app.widget.CdgWidget().updateAll(prefs.context)
            com.taxiparis.app.widget.OryWidget().updateAll(prefs.context)
            com.taxiparis.app.widget.NewMetroWidget().updateAll(prefs.context)
        }
    }

    fun refreshAll(includeTraffic: Boolean = true) {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            syncData(forceMetro = includeTraffic)

            val state = prefs.taxiState.first()
            if (state.metroData.isNotEmpty()) {
                try {
                    val lines = Json.decodeFromString<List<LineStatus>>(state.metroData)
                    _trafficState.value = TrafficState.Success(lines)
                } catch (e: Exception) {
                    _trafficState.value = TrafficState.Error("Erreur de décodage")
                }
            }

            updateWidgets()

            _isRefreshing.value = false
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(completed)
        }
    }

    fun setCGUAccepted(accepted: Boolean) {
        viewModelScope.launch {
            prefs.setCGUAccepted(accepted)
        }
    }

    fun setLocationOnboardingSeen(seen: Boolean) {
        viewModelScope.launch {
            prefs.setLocationOnboardingSeen(seen)
        }
    }

    fun setNotificationOnboardingSeen(seen: Boolean) {
        viewModelScope.launch {
            prefs.setNotificationOnboardingSeen(seen)
        }
    }

    fun toggleLine(lineId: String) {
        viewModelScope.launch {
            prefs.toggleLine(lineId)
        }
    }

    fun setWidgetTransparent(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setWidgetTransparent(enabled)
        }
    }

    fun setMaxTransparency(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setMaxTransparency(enabled)
        }
    }

    fun setNavigationApp(choice: Int) {
        viewModelScope.launch {
            prefs.setNavigationApp(choice)
        }
    }

    fun setThresholds(cdg: Int, ory: Int) {
        viewModelScope.launch {
            prefs.setThresholds(cdg, ory)
        }
    }

    fun setWidgetFontSize(size: Int) {
        viewModelScope.launch {
            prefs.setWidgetFontSize(size)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setVibrationEnabled(enabled)
        }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch {
            prefs.setRefreshInterval(minutes)
        }
    }

    fun setWorkingHours(start: Int, end: Int) {
        viewModelScope.launch {
            prefs.setWorkingHours(start, end)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            prefs.clearCache()
        }
    }

    fun fullReset() {
        viewModelScope.launch {
            prefs.fullReset()
        }
    }

    fun setAppTheme(theme: Int) {
        viewModelScope.launch {
            prefs.setAppTheme(theme)
        }
    }

    fun setSmartRefresh(enabled: Boolean, distance: Int) {
        viewModelScope.launch {
            prefs.setSmartRefresh(enabled, distance)
        }
    }

    fun setWidgetTextColor(color: Int) {
        viewModelScope.launch {
            prefs.setWidgetTextColor(color)
        }
    }
}
