package com.tlpcraft.adbdesktop.feature.settings.contract

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode

sealed interface SettingsIntent {
    data class AdbPathChanged(val value: String) : SettingsIntent

    data object AdbPathSaved : SettingsIntent

    data object AdbPathReset : SettingsIntent

    data object AdbCheckClicked : SettingsIntent

    data class ThemeSelected(val mode: ThemeMode) : SettingsIntent
}
