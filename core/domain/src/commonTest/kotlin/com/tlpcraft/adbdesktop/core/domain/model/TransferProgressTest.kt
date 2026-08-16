package com.tlpcraft.adbdesktop.core.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TransferProgressTest : FunSpec({
    test("the fraction stays between 0 and 1, and is unknown without a total") {
        TransferProgress("a", 50, 200).fraction shouldBe 0.25f
        TransferProgress("a", 300, 200).fraction shouldBe 1f
        TransferProgress("a", 0, 0).fraction.shouldBeNull()
        TransferProgress("a", 10, null).fraction.shouldBeNull()
    }
})
