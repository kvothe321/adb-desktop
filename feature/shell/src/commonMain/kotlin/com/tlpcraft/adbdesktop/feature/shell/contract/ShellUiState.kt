package com.tlpcraft.adbdesktop.feature.shell.contract

data class ShellUiState(
    val session: SessionState = SessionState.NoDevice,
    val lines: List<TerminalLine> = emptyList(),
    val input: String = "",
) {
    val canSubmit: Boolean
        get() = session == SessionState.Running && input.isNotBlank()
}

sealed interface SessionState {
    data object NoDevice : SessionState

    data object Running : SessionState

    data object Ended : SessionState

    data class Failed(val details: String) : SessionState
}

data class TerminalLine(val id: Long, val text: String, val isCommand: Boolean)
