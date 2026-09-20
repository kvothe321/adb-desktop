package com.tlpcraft.adbdesktop.feature.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.TerminalSize
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.shell.RunShellSessionUseCase
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.feature.shell.terminal.TerminalEmulator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_ROWS = 24
private const val DEFAULT_COLUMNS = 80
private const val PASTE_START = "\u001b[200~"
private const val PASTE_END = "\u001b[201~"
private const val REDRAW = "\u000c"

/** One session per selected device. It survives switching sections, like a terminal left open in the background. */
class ShellViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val runShellSession: RunShellSessionUseCase,
    private val hostClipboard: HostClipboard,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShellUiState())
    val uiState: StateFlow<ShellUiState> = _uiState.asStateFlow()

    private val sessionRequests = MutableStateFlow(0)
    private val size = MutableStateFlow<TerminalSize?>(null)
    private var input = Channel<String>(Channel.UNLIMITED)
    private var terminal = newTerminal()

    init {
        runSessionForSelectedDevice()
    }

    fun reduceIntent(intent: ShellIntent) {
        when (intent) {
            is ShellIntent.InputTyped -> send(intent.text)
            ShellIntent.PasteRequested -> viewModelScope.launch { paste() }
            is ShellIntent.TerminalResized -> resize(TerminalSize(intent.rows, intent.columns))
            ShellIntent.ClearClicked -> clear()
            ShellIntent.RestartClicked -> sessionRequests.update { it + 1 }
        }
    }

    private fun runSessionForSelectedDevice() = viewModelScope.launch {
        combine(
            observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged(),
            size.filterNotNull().map { }.distinctUntilChanged(),
            sessionRequests,
        ) { serial, _, _ -> serial }.collectLatest { serial ->
            terminal = newTerminal()
            publish()
            if (serial == null) {
                _uiState.update { it.copy(session = SessionState.NoDevice) }
            } else {
                runSession(serial)
            }
        }
    }

    private suspend fun runSession(serial: String) {
        input = Channel(Channel.UNLIMITED)
        _uiState.update { it.copy(session = SessionState.Running) }
        runShellSession(RunShellSessionUseCase.Params(serial, input.receiveAsFlow(), size.filterNotNull())).collect { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    terminal.feed(outcome.data)
                    publish()
                }

                is Outcome.Failure -> {
                    _uiState.update { it.copy(session = SessionState.Failed(outcome.error.message)) }
                }
            }
        }
        _uiState.update { state -> if (state.session == SessionState.Running) state.copy(session = SessionState.Ended) else state }
    }

    private fun send(text: String) {
        if (_uiState.value.session == SessionState.Running) input.trySend(text)
    }

    private suspend fun paste() {
        val text = hostClipboard.readText()?.replace("\r\n", "\n")?.replace('\n', '\r') ?: return
        send(if (terminal.hasBracketedPaste) PASTE_START + text + PASTE_END else text)
    }

    private fun resize(newSize: TerminalSize) {
        size.value = newSize
        terminal.resize(newSize.rows, newSize.columns)
        publish()
    }

    /** The shell prints its prompt again on request, as the screen no longer shows it. */
    private fun clear() {
        terminal.clear()
        publish()
        send(REDRAW)
    }

    private fun newTerminal(): TerminalEmulator {
        val current = size.value
        return TerminalEmulator(current?.rows ?: DEFAULT_ROWS, current?.columns ?: DEFAULT_COLUMNS, onReply = ::send)
    }

    private fun publish() {
        _uiState.update { it.copy(rows = terminal.render(), hasApplicationCursorKeys = terminal.hasApplicationCursorKeys) }
    }
}
