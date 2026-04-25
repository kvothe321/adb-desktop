package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.withContext

/**
 * Desktop implementation of [AppsDataSource].
 *
 * Runs `adb -s <serial> shell pm list packages [flag]` to enumerate installed packages.
 *
 * Flag mapping:
 * - [AppFilter.ALL]      → no flag
 * - [AppFilter.USER]     → `-3`
 * - [AppFilter.SYSTEM]   → `-s`
 * - [AppFilter.DISABLED] → `-d`
 *
 * Each output line has the form `package:<packageName>`. Lines that do not match
 * this prefix are silently dropped (e.g. empty lines or unexpected ADB output).
 *
 * All [ProcessBuilder] I/O is confined to [DispatcherProvider.io].
 */
class AppsDataSourceImpl(
    private val dispatcherProvider: DispatcherProvider,
) : AppsDataSource {

    override suspend fun getApps(deviceSerial: String, filter: AppFilter): List<AppInfo> = withContext(dispatcherProvider.io) {
        val args = buildList {
            add("adb")
            add("-s")
            add(deviceSerial)
            add("shell")
            add("pm")
            add("list")
            add("packages")
            filterFlag(filter)?.let { add(it) }
        }

        val process = ProcessBuilder(args)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        output.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith(PACKAGE_PREFIX) }
            .map { AppInfo(packageName = it.removePrefix(PACKAGE_PREFIX)) }
            .sortedBy { it.packageName }
            .toList()
    }

    private fun filterFlag(filter: AppFilter): String? = when (filter) {
        AppFilter.ALL -> null
        AppFilter.USER -> "-3"
        AppFilter.SYSTEM -> "-s"
        AppFilter.DISABLED -> "-d"
    }

    private companion object {
        const val PACKAGE_PREFIX = "package:"
    }
}
