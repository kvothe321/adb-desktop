package com.tlpcraft.adbdesktop.feature.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.shell.RunShellSessionUseCase
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import kotlinx.coroutines.channels.Channel
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

/** One session per selected device. It survives switching sections, like a terminal left open in the background. */
class ShellViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val runShellSession: RunShellSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShellUiState())
    val uiState: StateFlow<ShellUiState> = _uiState.asStateFlow()

    private val sessionRequests = MutableStateFlow(0)
    private val terminal = TerminalBuffer()
    private val history = mutableListOf<String>()
    private var historyIndex = 0
    private var commands = Channel<String>(Channel.UNLIMITED)

    init {
        runSessionForSelectedDevice()
    }

    fun reduceIntent(intent: ShellIntent) {
        when (intent) {
            is ShellIntent.InputChanged -> _uiState.update { it.copy(input = intent.value) }
            ShellIntent.CommandSubmitted -> submitCommand()
            ShellIntent.PreviousCommandRequested -> showHistoryEntry(historyIndex - 1)
            ShellIntent.NextCommandRequested -> showHistoryEntry(historyIndex + 1)
            ShellIntent.ClearClicked -> updateTerminal { clear() }
            ShellIntent.RestartClicked -> sessionRequests.update { it + 1 }
        }
    }

    private fun runSessionForSelectedDevice() = viewModelScope.launch {
        combine(
            observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged(),
            sessionRequests,
        ) { serial, _ -> serial }.collectLatest { serial ->
            updateTerminal { clear() }
            if (serial == null) {
                _uiState.update { it.copy(session = SessionState.NoDevice) }
            } else {
                runSession(serial)
            }
        }
    }

    private suspend fun runSession(serial: String) {
        commands = Channel(Channel.UNLIMITED)
        _uiState.update { it.copy(session = SessionState.Running) }
        runShellSession(RunShellSessionUseCase.Params(serial, commands.receiveAsFlow())).collect { outcome ->
            when (outcome) {
                is Outcome.Success -> updateTerminal { addOutput(outcome.data) }
                is Outcome.Failure -> _uiState.update { it.copy(session = SessionState.Failed(outcome.error.message)) }
            }
        }
        _uiState.update { state -> if (state.session == SessionState.Running) state.copy(session = SessionState.Ended) else state }
    }

    private fun submitCommand() {
        val state = _uiState.value
        if (!state.canSubmit) return

        val command = state.input.trim()
        history += command
        historyIndex = history.size
        _uiState.update { it.copy(input = "") }
        updateTerminal { addCommand(command) }
        commands.trySend(command)
    }

    /** Moving past the newest entry leaves an empty prompt, like a shell does. */
    private fun showHistoryEntry(index: Int) {
        if (index !in 0..history.size) return
        historyIndex = index
        _uiState.update { it.copy(input = history.getOrElse(index) { "" }) }
    }

    private fun updateTerminal(change: TerminalBuffer.() -> Unit) {
        terminal.change()
        _uiState.update { it.copy(lines = terminal.snapshot()) }
    }
}
