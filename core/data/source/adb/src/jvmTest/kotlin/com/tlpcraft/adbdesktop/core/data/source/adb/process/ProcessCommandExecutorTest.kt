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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout

private val CANCEL_TIMEOUT = 20.seconds
private const val REPEATED_CANCELLATIONS = 25

private fun javaSource(className: String, body: String): String = File.createTempFile(className, ".java").let { temp ->
    val source = File(temp.parentFile, "$className.java")
    temp.delete()
    source.writeText("public class $className { public static void main(String[] a) throws Exception { $body } }")
    source.deleteOnExit()
    source.path
}

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

    test("cancelling a stream kills a process that never exits") {
        val endless = javaSource("EndlessOutput", """System.out.println("ready"); System.out.flush(); Thread.sleep(600000);""")

        withTimeout(CANCEL_TIMEOUT) {
            executor.stream(listOf(java, endless)).first() shouldContain "ready"
        }
    }

    test("killing a process in the middle of a read counts as cancellation, not as a failure") {
        val flood = javaSource("Flood", """while (true) { System.out.println("line"); }""")

        repeat(REPEATED_CANCELLATIONS) {
            withTimeout(CANCEL_TIMEOUT) { executor.stream(listOf(java, flood)).first() }.shouldNotBeBlank()
        }
    }

    test("interactive input reaches stdin and the output comes back") {
        val echo = javaSource("EchoLine", """System.out.println("got " + new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine());""")

        withTimeout(CANCEL_TIMEOUT) {
            executor.interact(listOf(java, echo), flowOf("hello\n")).toList().joinToString("") shouldContain "got hello"
        }
    }

    test("binary output is returned untouched") {
        val binary = javaSource("BinaryOutput", """System.out.write(new byte[] { (byte) 0x89, 0x50 }); System.out.flush();""")

        executor.executeBinary(listOf(java, binary)).stdout.toList() shouldBe listOf(0x89.toByte(), 0x50.toByte())
    }
})
