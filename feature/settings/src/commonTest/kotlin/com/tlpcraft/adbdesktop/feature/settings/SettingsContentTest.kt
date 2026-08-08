package com.tlpcraft.adbdesktop.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsIntent
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class SettingsContentTest : FunSpec({
    test("shows the adb in use and reports choices") {
        runComposeUiTest {
            val intents = mutableListOf<SettingsIntent>()
            setContent {
                AppTheme {
                    SettingsContent(uiState = SettingsUiState(adbStatus = AdbStatus.Found(AdbInfo("/usr/bin/adb", "37.0.0"))), onIntent = { intents += it })
                }
            }

            onNodeWithText("Using /usr/bin/adb").assertExists()
            onNodeWithText("Dark").performClick()
            onNodeWithText("Detect automatically").performClick()

            intents shouldContain SettingsIntent.ThemeSelected(ThemeMode.DARK)
            intents shouldContain SettingsIntent.AdbPathReset
        }
    }
})
