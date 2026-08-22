package com.tlpcraft.adbdesktop.core.data.repository.util

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

class InvalidationsTest : FunSpec({
    test("reads on collection and again as soon as it is invalidated") {
        val invalidations = Invalidations()
        var value = 1

        invalidations.observe(1.hours) { value }.test {
            awaitItem() shouldBe 1

            value = 2
            invalidations.invalidate()

            awaitItem() shouldBe 2
        }
    }

    test("the interval catches changes made elsewhere") {
        var value = 1

        Invalidations().observe(5.milliseconds) { value }.test {
            awaitItem() shouldBe 1

            value = 2

            awaitItem() shouldBe 2
        }
    }

    test("an unchanged value is not emitted again") {
        var reads = 0

        Invalidations().observe(5.milliseconds) {
            reads++
            "same"
        }.test {
            awaitItem() shouldBe "same"
            delay(50.milliseconds)
            expectNoEvents()
        }
        (reads > 1) shouldBe true
    }
})
