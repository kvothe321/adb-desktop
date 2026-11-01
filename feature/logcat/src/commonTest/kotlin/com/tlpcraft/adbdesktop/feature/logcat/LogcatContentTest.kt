package com.tlpcraft.adbdesktop.feature.logcat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogRow
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatIntent
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class LogcatContentTest : FunSpec({
    test("shows entries and reports the controls") {
        runComposeUiTest {
            val intents = mutableListOf<LogcatIntent>()
            val row = LogRow(id = 1, LogEntry("10-31 23:05:50.598", 1, 2, LogLevel.ERROR, "AndroidRuntime", "FATAL EXCEPTION: main"))
            setContent {
                AppTheme { LogcatContent(uiState = LogcatUiState(serial = "pixel", rows = listOf(row)), onIntent = { intents += it }) }
            }

            onNodeWithText("FATAL EXCEPTION: main").assertExists()
            onNodeWithText("Pause").performClick()
            onNodeWithText("Clear").performClick()
            onNodeWithText("Error").performClick()

            intents shouldContain LogcatIntent.PauseToggled
            intents shouldContain LogcatIntent.ClearClicked
            intents shouldContain LogcatIntent.LevelSelected(LogLevel.ERROR)
        }
    }
})
