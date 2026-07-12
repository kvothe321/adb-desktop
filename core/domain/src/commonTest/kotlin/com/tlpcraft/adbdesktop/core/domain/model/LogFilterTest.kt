package com.tlpcraft.adbdesktop.core.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class LogFilterTest : FunSpec({
    val entry = LogEntry("10-31 23:05:50.598", 1, 2, LogLevel.WARNING, "ActivityManager", "Slow operation")

    test("hides entries below the minimum level") {
        LogFilter(minimumLevel = LogLevel.INFO).matches(entry).shouldBeTrue()
        LogFilter(minimumLevel = LogLevel.ERROR).matches(entry).shouldBeFalse()
    }

    test("the query matches tag or message, ignoring case") {
        LogFilter(query = "activitymanager").matches(entry).shouldBeTrue()
        LogFilter(query = "SLOW").matches(entry).shouldBeTrue()
        LogFilter(query = "bluetooth").matches(entry).shouldBeFalse()
    }
})
