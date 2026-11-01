package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbLogcatDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbLogcatDataSource.Script
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

class LogcatRepositoryImplTest : FunSpec({
    fun entry(time: String, message: String) = LogEntryDto("11-01 10:00:0$time.000", 1, 1, 'I', "Tag", message)

    fun messagesOf(outcome: Outcome<List<LogEntry>, DeviceError>) = outcome.shouldBeInstanceOf<Outcome.Success<List<LogEntry>>>().data.map { it.message }

    test("a broken stream is reported, then resumes after the last entry without repeating any") {
        val source = FakeAdbLogcatDataSource(
            Script(listOf(listOf(entry("1", "a"), entry("2", "b"), entry("2", "c"))), failure = "device offline"),
            Script(listOf(listOf(entry("2", "b"), entry("2", "c"), entry("2", "d"), entry("3", "e")))),
        )

        runTest {
            LogcatRepositoryImpl(source).observeLogs("pixel").test {
                messagesOf(awaitItem()) shouldContainExactly listOf("a", "b", "c")
                awaitItem().shouldBeInstanceOf<Outcome.Failure<*>>()
                messagesOf(awaitItem()) shouldContainExactly listOf("d", "e")
                cancelAndIgnoreRemainingEvents()
            }
        }
        source.requestedSince.take(2) shouldContainExactly listOf(null, "11-01 10:00:02.000")
    }

    test("a stream that ends on its own reconnects without reporting a failure") {
        val source = FakeAdbLogcatDataSource(
            Script(listOf(listOf(entry("1", "a")))),
            Script(listOf(listOf(entry("1", "a"), entry("2", "b")))),
        )

        runTest {
            LogcatRepositoryImpl(source).observeLogs("pixel").test {
                messagesOf(awaitItem()) shouldContainExactly listOf("a")
                messagesOf(awaitItem()) shouldContainExactly listOf("b")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    test("identical lines in one stream are all kept, and only as many are skipped on the way back") {
        val source = FakeAdbLogcatDataSource(
            Script(listOf(listOf(entry("1", "same"), entry("1", "same"))), failure = "gone"),
            Script(listOf(listOf(entry("1", "same"), entry("1", "same"), entry("1", "same")))),
        )

        runTest {
            LogcatRepositoryImpl(source).observeLogs("pixel").test {
                messagesOf(awaitItem()) shouldContainExactly listOf("same", "same")
                awaitItem().shouldBeInstanceOf<Outcome.Failure<*>>()
                messagesOf(awaitItem()) shouldContainExactly listOf("same")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
})
