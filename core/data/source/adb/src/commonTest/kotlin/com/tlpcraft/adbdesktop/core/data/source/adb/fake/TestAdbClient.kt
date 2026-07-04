package com.tlpcraft.adbdesktop.core.data.source.adb.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbBinaryResolver
import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.logger.AppLogger

private object SilentLogger : AppLogger {
    override fun debug(tag: String, message: String) = Unit

    override fun info(tag: String, message: String) = Unit

    override fun warn(tag: String, message: String, throwable: Throwable?) = Unit

    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}

fun testAdbClient(executor: FakeCommandExecutor): AdbClient {
    val environment = FakeAdbEnvironment(variables = mapOf("PATH" to "/usr/bin"), executables = setOf("/usr/bin/adb"))
    return AdbClient(AdbBinaryResolver(environment) { null }, executor, SilentLogger)
}
