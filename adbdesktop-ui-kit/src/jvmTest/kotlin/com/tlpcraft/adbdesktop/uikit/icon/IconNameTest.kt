package com.tlpcraft.adbdesktop.uikit.icon

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class IconNameTest : FunSpec({
    IconName.entries.forEach { icon ->
        test("$icon is a parsed glyph in a 24 box") {
            icon.vector.viewportWidth shouldBe 24f
            icon.vector.viewportHeight shouldBe 24f
            icon.vector.root.size shouldNotBe 0
        }
    }
})
