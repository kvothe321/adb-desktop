package com.tlpcraft.adbdesktop.core.data.source.adb.helper

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.LineBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.random.Random
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Runs the device helper bundled with the app. Each run pushes it under a name of its own and deletes it afterwards,
 * so runs never trip over each other and nothing is left on the device.
 */
class DeviceHelper(private val client: AdbClient) {
    private val helperFile: Path by lazy(::extractHelper)

    /** The output lines of the helper's [mainClass], run once for each list in [argumentRuns], until it exits or is cancelled. */
    fun run(serial: String, mainClass: String, argumentRuns: List<List<String>>): Flow<String> = flow {
        val remotePath = "/data/local/tmp/adb-desktop-helper-${Random.nextInt(1, Int.MAX_VALUE).toString(radix = 16)}.apk"
        client.run(listOf("-s", serial, "push", helperFile.toString(), remotePath))
        try {
            argumentRuns.forEach { arguments ->
                val lines = LineBuffer()
                client.stream(listOf("-s", serial, "shell", "CLASSPATH=$remotePath", "app_process", "/", "$PACKAGE.$mainClass") + arguments)
                    .collect { output -> lines.feed(output).forEach { emit(it) } }
            }
        } finally {
            withContext(NonCancellable) { client.execute("-s", serial, "shell", "rm", "-f", remotePath) }
        }
    }

    private fun extractHelper(): Path {
        val file = Files.createTempFile("adb-desktop-device-helper", ".apk").also { it.toFile().deleteOnExit() }
        checkNotNull(javaClass.getResourceAsStream(RESOURCE)) { "The device helper is missing from this build" }.use { resource ->
            Files.copy(resource, file, StandardCopyOption.REPLACE_EXISTING)
        }
        return file
    }

    private companion object {
        const val RESOURCE = "/device-helper.apk"
        const val PACKAGE = "com.tlpcraft.adbdesktop.helper"
    }
}
