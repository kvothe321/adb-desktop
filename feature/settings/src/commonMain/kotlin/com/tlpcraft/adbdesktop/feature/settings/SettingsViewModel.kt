package com.tlpcraft.adbdesktop.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ReadAdbInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsIntent
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val observeAdbPathOverride: ObserveAdbPathOverrideUseCase,
    private val setAdbPathOverride: SetAdbPathOverrideUseCase,
    private val readAdbInfo: ReadAdbInfoUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(adbPathInput = observeAdbPathOverride(Unit).first().orEmpty()) }
            checkAdb()
        }
        viewModelScope.launch {
            observeThemeMode(Unit).collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
    }

    fun reduceIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.AdbPathChanged -> _uiState.update { it.copy(adbPathInput = intent.value) }
            SettingsIntent.AdbPathSaved -> saveAdbPath(_uiState.value.adbPathInput)
            SettingsIntent.AdbPathReset -> saveAdbPath(path = null)
            SettingsIntent.AdbCheckClicked -> viewModelScope.launch { checkAdb() }
            is SettingsIntent.ThemeSelected -> viewModelScope.launch { setThemeMode(intent.mode) }
        }
    }

    private fun saveAdbPath(path: String?) {
        viewModelScope.launch {
            setAdbPathOverride(path)
            _uiState.update { it.copy(adbPathInput = path.orEmpty()) }
            checkAdb()
        }
    }

    private suspend fun checkAdb() {
        _uiState.update { it.copy(adbStatus = AdbStatus.Checking) }
        val status = when (val result = readAdbInfo(Unit)) {
            is Outcome.Success -> AdbStatus.Found(result.data)
            is Outcome.Failure -> AdbStatus.Unavailable(result.error.message)
        }
        _uiState.update { it.copy(adbStatus = status) }
    }
}
