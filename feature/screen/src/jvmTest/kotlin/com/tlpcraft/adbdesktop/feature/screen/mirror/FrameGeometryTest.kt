package com.tlpcraft.adbdesktop.feature.screen.mirror

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.tlpcraft.adbdesktop.core.domain.model.MirrorPoint
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class FrameGeometryTest : FunSpec({
    val box = IntSize(400, 400)

    test("a tall frame is centred with bars on the sides and scaled to the box height") {
        framePointAt(Offset(200f, 200f), box, frameWidth = 100, frameHeight = 200, isClamped = false) shouldBe MirrorPoint(50, 100, 100, 200)
        framePointAt(Offset(100f, 0f), box, frameWidth = 100, frameHeight = 200, isClamped = false) shouldBe MirrorPoint(0, 0, 100, 200)
    }

    test("a press on the bars misses the frame") {
        framePointAt(Offset(50f, 200f), box, frameWidth = 100, frameHeight = 200, isClamped = false).shouldBeNull()
    }

    test("a drag past the frame ends on its nearest edge") {
        framePointAt(Offset(390f, -20f), box, frameWidth = 100, frameHeight = 200, isClamped = true) shouldBe MirrorPoint(99, 0, 100, 200)
    }

    test("nothing maps before the box or frame has a size") {
        framePointAt(Offset(1f, 1f), IntSize.Zero, frameWidth = 100, frameHeight = 200, isClamped = true).shouldBeNull()
    }
})
