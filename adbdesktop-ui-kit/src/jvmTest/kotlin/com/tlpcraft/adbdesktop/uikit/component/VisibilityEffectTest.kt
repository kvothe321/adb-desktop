package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

@OptIn(ExperimentalTestApi::class)
class VisibilityEffectTest : FunSpec({
    test("reports the screen as visible when it opens and hidden when it is left") {
        runComposeUiTest {
            var isOnScreen by mutableStateOf(true)
            val reports = mutableListOf<Boolean>()
            setContent {
                if (isOnScreen) VisibilityEffect { reports += it }
            }

            waitForIdle()
            reports shouldContainExactly listOf(true)

            isOnScreen = false
            waitForIdle()
            reports shouldContainExactly listOf(true, false)
        }
    }
})
