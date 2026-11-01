package com.tlpcraft.adbdesktop.feature.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.feature.screen.contract.RecordingState
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class ScreenContentTest : FunSpec({
    test("capture and record are offered with a device") {
        runComposeUiTest {
            val intents = mutableListOf<ScreenIntent>()
            setContent {
                AppTheme { ScreenContent(uiState = ScreenUiState(serial = "pixel"), onIntent = { intents += it }) }
            }

            onNodeWithText("Take screenshot").performClick()
            onNodeWithText("Record").performClick()

            intents shouldContain ScreenIntent.CaptureClicked
            intents shouldContain ScreenIntent.RecordClicked
        }
    }

    test("a running recording can be stopped") {
        runComposeUiTest {
            val intents = mutableListOf<ScreenIntent>()
            setContent {
                AppTheme {
                    ScreenContent(uiState = ScreenUiState(serial = "pixel", recording = RecordingState.Recording, recordingSeconds = 75), onIntent = {
                        intents +=
                            it
                    })
                }
            }

            onNodeWithText("Recording 01:15").assertExists()
            onNodeWithText("Stop").performClick()

            intents shouldContain ScreenIntent.StopClicked
        }
    }
})
