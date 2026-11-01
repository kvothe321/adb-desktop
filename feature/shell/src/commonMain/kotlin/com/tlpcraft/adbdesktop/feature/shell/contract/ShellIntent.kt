package com.tlpcraft.adbdesktop.feature.shell.contract

sealed interface ShellIntent {
    /** [text] is what the terminal sends, control codes included. */
    data class InputTyped(val text: String) : ShellIntent

    data object PasteRequested : ShellIntent

    data class TerminalResized(val rows: Int, val columns: Int) : ShellIntent

    data object ClearClicked : ShellIntent

    data object RestartClicked : ShellIntent
}
