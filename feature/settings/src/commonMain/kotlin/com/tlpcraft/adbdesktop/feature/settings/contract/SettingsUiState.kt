package com.tlpcraft.adbdesktop.feature.settings.contract

import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode

data class SettingsUiState(
    val adbStatus: AdbStatus = AdbStatus.Checking,
    val adbPathInput: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

sealed interface AdbStatus {
    data object Checking : AdbStatus

    data class Found(val info: AdbInfo) : AdbStatus

    data class Unavailable(val details: String) : AdbStatus
}
