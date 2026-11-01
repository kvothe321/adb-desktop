package com.tlpcraft.adbdesktop.core.data.source.preferences

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class SettingsDirectoryTest : FunSpec({
    test("Linux uses the XDG config directory") {
        settingsDirectory("Linux", "/home/me") { "/custom".takeIf { _ -> it == "XDG_CONFIG_HOME" } } shouldBe File("/custom/adb-desktop")
    }

    test("Linux falls back to dot config") {
        settingsDirectory("Linux", "/home/me") { null } shouldBe File("/home/me/.config/adb-desktop")
    }

    test("macOS uses Application Support") {
        settingsDirectory("Mac OS X", "/Users/me") { null } shouldBe File("/Users/me/Library/Application Support/ADB Desktop")
    }

    test("Windows uses the roaming application data") {
        settingsDirectory("Windows 11", "C:/Users/me") { "C:/Roaming".takeIf { _ -> it == "APPDATA" } } shouldBe File("C:/Roaming/ADB Desktop")
    }
})
