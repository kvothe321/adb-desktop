package com.tlpcraft.adbdesktop.feature.shell.contract

sealed interface ShellIntent {
    data class InputChanged(val value: String) : ShellIntent

    data object CommandSubmitted : ShellIntent

    data object PreviousCommandRequested : ShellIntent

    data object NextCommandRequested : ShellIntent

    data object ClearClicked : ShellIntent

    data object RestartClicked : ShellIntent
}
