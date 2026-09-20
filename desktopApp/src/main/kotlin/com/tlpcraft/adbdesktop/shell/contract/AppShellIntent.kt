package com.tlpcraft.adbdesktop.shell.contract

sealed interface AppShellIntent {
    data class DeviceSelected(val serial: String) : AppShellIntent

    /** The sidebar toggle always picks an explicit theme, the opposite of the one showing. */
    data class ThemeToggled(val isDarkShowing: Boolean) : AppShellIntent
}
