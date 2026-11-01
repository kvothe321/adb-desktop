package com.tlpcraft.adbdesktop.core.common.flow

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

class WhileActiveTest : FunSpec({
    test("the source runs only while active, and restarts when activated again") {
        val active = MutableStateFlow(false)
        val source = MutableStateFlow(1)

        source.whileActive(active).test {
            expectNoEvents()

            active.value = true
            awaitItem() shouldBe 1
            source.value = 2
            awaitItem() shouldBe 2

            active.value = false
            source.value = 3
            expectNoEvents()

            active.value = true
            awaitItem() shouldBe 3
        }
    }
})
