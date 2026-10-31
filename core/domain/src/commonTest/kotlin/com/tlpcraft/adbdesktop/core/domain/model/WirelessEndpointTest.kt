package com.tlpcraft.adbdesktop.core.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class WirelessEndpointTest : FunSpec({
    context("valid input") {
        withTests(
            "192.168.1.42:5555" to WirelessEndpoint("192.168.1.42", 5555),
            "  192.168.1.42:37123 " to WirelessEndpoint("192.168.1.42", 37123),
            "pixel.local:5555" to WirelessEndpoint("pixel.local", 5555),
            "[fe80::1]:5555" to WirelessEndpoint("fe80::1", 5555),
        ) { (input, expected) ->
            WirelessEndpoint.parse(input) shouldBe expected
        }
    }

    test("uses the default port when none is given") {
        WirelessEndpoint.parse("192.168.1.42", defaultPort = 5555) shouldBe WirelessEndpoint("192.168.1.42", 5555)
        WirelessEndpoint.parse("[fe80::1]", defaultPort = 5555) shouldBe WirelessEndpoint("fe80::1", 5555)
    }

    context("invalid input") {
        withTests(
            nameFn = { "'$it'" },
            "",
            " ",
            "192.168.1.42",
            "192.168.1.42:",
            "192.168.1.42:abc",
            "192.168.1.42:70000",
            ":5555",
            "fe80::1",
            "a b:5555",
            "[fe80::1]x",
        ) {
            WirelessEndpoint.parse(it).shouldBeNull()
        }
    }

    test("formats IPv6 hosts with brackets") {
        WirelessEndpoint("fe80::1", 5555).toString() shouldBe "[fe80::1]:5555"
        WirelessEndpoint("10.0.0.2", 5555).toString() shouldBe "10.0.0.2:5555"
    }
})
