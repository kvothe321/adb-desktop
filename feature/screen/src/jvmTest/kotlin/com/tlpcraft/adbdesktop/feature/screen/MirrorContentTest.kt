package com.tlpcraft.adbdesktop.feature.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction
import com.tlpcraft.adbdesktop.feature.screen.contract.DeviceButton
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalTestApi::class)
class MirrorContentTest : FunSpec({
    val frame = MirrorFrame(width = 10, height = 20, pixels = ByteArray(10 * 20 * 4))
    val live = MirrorUiState(serial = "pixel", isOn = true, mirror = MirrorState.Live(frame))

    test("mirroring starts from the empty phone and stops from the header") {
        runComposeUiTest {
            val intents = mutableListOf<MirrorIntent>()
            var state by mutableStateOf(MirrorUiState(serial = "pixel"))
            setContent { AppTheme { MirrorContent(uiState = state, onIntent = { intents += it }) } }

            onNodeWithText("Start mirroring").performClick()
            state = live
            onNodeWithText("Stop").performClick()

            intents shouldContainExactly listOf(MirrorIntent.MirrorClicked, MirrorIntent.MirrorClicked)
        }
    }

    test("the device buttons press the device's own keys") {
        runComposeUiTest {
            val intents = mutableListOf<MirrorIntent>()
            setContent { AppTheme { MirrorContent(uiState = live, onIntent = { intents += it }) } }

            onNodeWithContentDescription("Home").performClick()

            intents shouldContain MirrorIntent.DeviceButtonClicked(DeviceButton.Home)
        }
    }

    test("a click on the screen is a tap at that spot of the frame") {
        runComposeUiTest {
            val inputs = mutableListOf<MirrorInput>()
            setContent { AppTheme { MirrorContent(uiState = live, onIntent = { (it as? MirrorIntent.InputGiven)?.let { given -> inputs += given.input } }) } }

            onNodeWithContentDescription("Live view of the device screen").performMouseInput { click(center) }

            inputs.map { (it as MirrorInput.Touch).action } shouldBe listOf(TouchAction.Down, TouchAction.Up)
            val point = (inputs.first() as MirrorInput.Touch).point
            point.frameWidth shouldBe 10
            point.frameHeight shouldBe 20
            point.x shouldBe 5
            point.y shouldBe 10
        }
    }

    test("a right click is back") {
        runComposeUiTest {
            val inputs = mutableListOf<MirrorInput>()
            setContent { AppTheme { MirrorContent(uiState = live, onIntent = { (it as? MirrorIntent.InputGiven)?.let { given -> inputs += given.input } }) } }

            onNodeWithContentDescription("Live view of the device screen").performMouseInput { rightClick(center) }

            inputs shouldContainExactly listOf(MirrorInput.BackOrScreenOn)
        }
    }
})
