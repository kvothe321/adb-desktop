package com.tlpcraft.adbdesktop.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.flow.whileActive
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsIntent
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val observeAdbPathOverride: ObserveAdbPathOverrideUseCase,
    private val setAdbPathOverride: SetAdbPathOverrideUseCase,
    private val observeAdbInfo: ObserveAdbInfoUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val isVisible = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            observeAdbPathOverride(Unit).collect { path -> _uiState.update { it.copy(adbPathInput = path.orEmpty()) } }
        }
        viewModelScope.launch {
            observeAdbInfo(Unit).whileActive(isVisible).collect { outcome ->
                val status = when (outcome) {
                    is Outcome.Success -> AdbStatus.Found(outcome.data)
                    is Outcome.Failure -> AdbStatus.Unavailable(outcome.error.message)
                }
                _uiState.update { it.copy(adbStatus = status) }
            }
        }
        viewModelScope.launch {
            observeThemeMode(Unit).collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
    }

    fun reduceIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.VisibilityChanged -> isVisible.value = intent.isVisible
            is SettingsIntent.AdbPathChanged -> _uiState.update { it.copy(adbPathInput = intent.value) }
            SettingsIntent.AdbPathSaved -> viewModelScope.launch { setAdbPathOverride(_uiState.value.adbPathInput) }
            SettingsIntent.AdbPathReset -> resetAdbPath()
            is SettingsIntent.ThemeSelected -> viewModelScope.launch { setThemeMode(intent.mode) }
        }
    }

    private fun resetAdbPath() {
        _uiState.update { it.copy(adbPathInput = "") }
        viewModelScope.launch { setAdbPathOverride(null) }
    }
}
