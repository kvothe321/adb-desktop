package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalTestApi::class)
class PixelImageTest : FunSpec({
    test("reads pixels as BGRA with the top row first") {
        val bluePixel = byteArrayOf(-1, 0, 0, -1)
        val redPixel = byteArrayOf(0, 0, -1, -1)

        val bitmap = imageBitmapOfBgra(width = 1, height = 2, pixels = bluePixel + redPixel)

        runComposeUiTest {
            val pixels = IntArray(2).also { bitmap.readPixels(it) }
            Color(pixels[0]) shouldBe Color.Blue
            Color(pixels[1]) shouldBe Color.Red
        }
    }
})
