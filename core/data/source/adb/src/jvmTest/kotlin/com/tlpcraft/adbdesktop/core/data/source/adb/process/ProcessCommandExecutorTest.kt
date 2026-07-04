package com.tlpcraft.adbdesktop.core.data.source.adb.process

import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList

/** Runs the current JVM's own `java` launcher, which exists on every host the tests run on. */
@Tags("platform")
class ProcessCommandExecutorTest : FunSpec({
    val java = File(System.getProperty("java.home"), "bin/java").path
    val executor = ProcessCommandExecutor(
        object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
            override val io: CoroutineDispatcher = Dispatchers.IO
            override val default: CoroutineDispatcher = Dispatchers.Default
        },
    )

    test("captures stdout and the exit code") {
        val result = executor.execute(listOf(java, "--version"))

        result.exitCode shouldBe 0
        result.stdout.shouldNotBeBlank()
    }

    test("captures stderr of a failing command") {
        val result = executor.execute(listOf(java, "--no-such-option"))

        (result.exitCode != 0) shouldBe true
        result.stderr shouldContain "no-such-option"
    }

    test("streams stdout until the process exits") {
        executor.stream(listOf(java, "--version")).toList().joinToString("").shouldNotBeBlank()
    }

    test("a failing stream reports the exit code") {
        shouldThrow<AdbException.CommandFailed> { executor.stream(listOf(java, "--no-such-option")).toList() }
    }
})
