package com.tlpcraft.adbdesktop.core.data.source.adb.helper

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.LineBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64
import kotlin.random.Random
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Reads icons with the device helper, which asks Android's PackageManager and draws each icon to a PNG. Each read pushes
 * the helper under a name of its own and deletes it afterwards, so reads never trip over each other.
 */
class HelperAppIconDataSource(private val client: AdbClient) : AdbAppIconDataSource {
    private val helperFile: Path by lazy(::extractHelper)

    override fun streamIcons(serial: String, packageNames: List<String>): Flow<Pair<String, ByteArray?>> = flow {
        val valid = packageNames.filter { it.matches(PACKAGE_NAME) }
        if (valid.isEmpty()) return@flow
        val remotePath = "/data/local/tmp/adb-desktop-helper-${Random.nextInt(1, Int.MAX_VALUE).toString(radix = 16)}.apk"
        client.run(listOf("-s", serial, "push", helperFile.toString(), remotePath))
        try {
            valid.chunked(PACKAGES_PER_RUN).forEach { chunk ->
                val lines = LineBuffer()
                client.stream(listOf("-s", serial, "shell", "CLASSPATH=$remotePath", "app_process", "/", MAIN_CLASS, ICON_SIZE.toString()) + chunk)
                    .collect { output -> lines.feed(output).mapNotNull(::parseLine).forEach { emit(it) } }
            }
        } finally {
            withContext(NonCancellable) { client.execute("-s", serial, "shell", "rm", "-f", remotePath) }
        }
    }

    private fun parseLine(line: String): Pair<String, ByteArray?>? {
        val packageName = line.substringBefore(' ')
        val icon = line.substringAfter(' ', missingDelimiterValue = "")
        if (!packageName.matches(PACKAGE_NAME) || icon.isEmpty()) return null
        return packageName to icon.takeUnless { it == NO_ICON }?.let { runCatching { Base64.getDecoder().decode(it) }.getOrNull() }
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
        const val MAIN_CLASS = "com.tlpcraft.adbdesktop.helper.AppIcons"
        const val NO_ICON = "-"

        /** Big enough for the largest place an icon is shown, on a screen with twice the density. */
        const val ICON_SIZE = 96

        /** Keeps the command line well under the shell's limit. */
        const val PACKAGES_PER_RUN = 100
        val PACKAGE_NAME = Regex("[A-Za-z0-9_.]+")
    }
}
