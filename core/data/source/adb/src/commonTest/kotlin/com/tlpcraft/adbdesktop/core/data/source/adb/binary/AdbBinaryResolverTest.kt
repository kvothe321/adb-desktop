package com.tlpcraft.adbdesktop.core.data.source.adb.binary

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.FakeAdbEnvironment
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class AdbBinaryResolverTest : FunSpec({
    val sdkVariables = mapOf(
        "ANDROID_HOME" to "/home/me/Android/Sdk/",
        "ANDROID_SDK_ROOT" to "/opt/android-sdk",
        "PATH" to "/usr/local/bin:/usr/bin",
    )

    fun resolver(environment: AdbEnvironment, override: String? = null) = AdbBinaryResolver(environment) { override }

    test("prefers ANDROID_HOME over ANDROID_SDK_ROOT and PATH") {
        val environment = FakeAdbEnvironment(sdkVariables, executables = setOf("/home/me/Android/Sdk/platform-tools/adb", "/usr/bin/adb"))

        resolver(environment).resolve() shouldBe "/home/me/Android/Sdk/platform-tools/adb"
    }

    test("falls back to PATH") {
        val environment = FakeAdbEnvironment(sdkVariables, executables = setOf("/usr/bin/adb"))

        resolver(environment).resolve() shouldBe "/usr/bin/adb"
    }

    test("uses the Windows executable name and separators") {
        val environment = FakeAdbEnvironment(
            variables = mapOf("PATH" to "C:\\tools;C:\\Android\\platform-tools"),
            executables = setOf("C:\\Android\\platform-tools\\adb.exe"),
            executableName = "adb.exe",
            pathSeparator = ";",
            fileSeparator = "\\",
        )

        resolver(environment).resolve() shouldBe "C:\\Android\\platform-tools\\adb.exe"
    }

    test("the user override wins") {
        val environment = FakeAdbEnvironment(sdkVariables, executables = setOf("/custom/adb", "/usr/bin/adb"))

        resolver(environment, override = "/custom/adb").resolve() shouldBe "/custom/adb"
    }

    test("an invalid override fails instead of falling back") {
        val environment = FakeAdbEnvironment(sdkVariables, executables = setOf("/usr/bin/adb"))

        shouldThrow<AdbException.InvalidExecutable> { resolver(environment, override = "/missing/adb").resolve() }.path shouldBe "/missing/adb"
    }

    test("reports every searched location when adb is missing") {
        val error = shouldThrow<AdbException.ExecutableNotFound> { resolver(FakeAdbEnvironment(sdkVariables)).resolve() }

        error.searchedLocations shouldContainExactly listOf(
            "/home/me/Android/Sdk/platform-tools/adb",
            "/opt/android-sdk/platform-tools/adb",
            "/usr/local/bin/adb",
            "/usr/bin/adb",
        )
    }
})
