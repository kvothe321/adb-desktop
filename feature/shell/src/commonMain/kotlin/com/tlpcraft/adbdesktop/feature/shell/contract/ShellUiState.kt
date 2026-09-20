package com.tlpcraft.adbdesktop.feature.shell.contract

import com.tlpcraft.adbdesktop.uikit.component.TerminalRow

data class ShellUiState(
    val session: SessionState = SessionState.NoDevice,
    val rows: List<TerminalRow> = emptyList(),
    val hasApplicationCursorKeys: Boolean = false,
)

sealed interface SessionState {
    data object NoDevice : SessionState

    data object Running : SessionState

    data object Ended : SessionState

    data class Failed(val details: String) : SessionState
}
