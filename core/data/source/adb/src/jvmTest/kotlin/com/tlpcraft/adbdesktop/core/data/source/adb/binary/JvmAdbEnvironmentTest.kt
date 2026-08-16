package com.tlpcraft.adbdesktop.core.data.source.adb.binary

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.io.File

@Tags("platform")
class JvmAdbEnvironmentTest : FunSpec({
    val environment = JvmAdbEnvironment()
    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    test("uses the host's executable name and separators") {
        environment.executableName shouldBe if (isWindows) "adb.exe" else "adb"
        environment.pathSeparator shouldBe File.pathSeparator
        environment.fileSeparator shouldBe File.separator
    }

    test("reads environment variables") {
        environment.variable("PATH") shouldBe System.getenv("PATH")
        environment.variable("ADB_DESKTOP_UNSET_VARIABLE_FOR_TESTS").shouldBe(null)
    }

    test("reports the size of files, not folders, and deletes them") {
        val file = File(tempdir(), "file.txt").apply { writeText("four") }

        environment.fileSize(file.path) shouldBe 4L
        environment.fileSize(tempdir().path) shouldBe null

        environment.deleteFile(file.path)
        environment.fileSize(file.path) shouldBe null
    }

    test("accepts executable files only") {
        val executable = tempfile().apply { setExecutable(true) }

        environment.isExecutableFile(executable.path).shouldBeTrue()
        environment.isExecutableFile(tempdir().path).shouldBeFalse()
        environment.isExecutableFile(File(tempdir(), "missing").path).shouldBeFalse()
    }
})
