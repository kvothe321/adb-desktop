package com.tlpcraft.adbdesktop.feature.logcat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ClearLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveLogcatUseCase
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogRow
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatIntent
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatSideEffect
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Streams only while the screen is open, so a background logcat process never runs unnoticed. */
class LogcatViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val observeLogcat: ObserveLogcatUseCase,
    private val clearLogcat: ClearLogcatUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogcatUiState())
    val uiState: StateFlow<LogcatUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<LogcatSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<LogcatSideEffect> = _sideEffects.receiveAsFlow()

    private val isScreenOpen = MutableStateFlow(false)
    private val streamRequests = MutableStateFlow(0)
    private val buffer = ArrayDeque<LogRow>()
    private var nextRowId = 0L

    init {
        streamWhileOpen()
    }

    fun reduceIntent(intent: LogcatIntent) {
        when (intent) {
            LogcatIntent.ScreenOpened -> isScreenOpen.value = true
            LogcatIntent.ScreenClosed -> isScreenOpen.value = false
            is LogcatIntent.LevelSelected -> _uiState.update { it.copy(filter = it.filter.copy(minimumLevel = intent.level)) }
            is LogcatIntent.QueryChanged -> _uiState.update { it.copy(filter = it.filter.copy(query = intent.value)) }
            LogcatIntent.PauseToggled -> togglePause()
            LogcatIntent.ClearClicked -> clear()
            LogcatIntent.RetryClicked -> streamRequests.update { it + 1 }
        }
    }

    private fun streamWhileOpen() = viewModelScope.launch {
        combine(
            observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged(),
            isScreenOpen,
            streamRequests,
        ) { serial, isOpen, _ -> serial to isOpen }.collectLatest { (serial, isOpen) ->
            _uiState.update { it.copy(serial = serial, failure = null) }
            if (serial != null && isOpen) stream(serial)
        }
    }

    private suspend fun stream(serial: String) {
        buffer.clear()
        publish()
        observeLogcat(serial).collect { outcome ->
            when (outcome) {
                is Outcome.Success -> append(outcome.data)
                is Outcome.Failure -> _uiState.update { it.copy(failure = outcome.error.message) }
            }
        }
    }

    private fun append(entries: List<LogEntry>) {
        entries.forEach { buffer.addLast(LogRow(nextRowId++, it)) }
        repeat((buffer.size - MAX_ROWS).coerceAtLeast(0)) { buffer.removeFirst() }
        if (!_uiState.value.isPaused) publish()
    }

    private fun publish() {
        _uiState.update { it.copy(rows = buffer.toList()) }
    }

    private fun togglePause() {
        val resumes = _uiState.value.isPaused
        _uiState.update { it.copy(isPaused = !resumes) }
        if (resumes) publish()
    }

    private fun clear() {
        val serial = _uiState.value.serial ?: return
        viewModelScope.launch {
            when (clearLogcat(serial)) {
                is Outcome.Success -> {
                    buffer.clear()
                    publish()
                }

                is Outcome.Failure -> {
                    _sideEffects.send(LogcatSideEffect.ClearFailed)
                }
            }
        }
    }

    private companion object {
        const val MAX_ROWS = 5_000
    }
}
