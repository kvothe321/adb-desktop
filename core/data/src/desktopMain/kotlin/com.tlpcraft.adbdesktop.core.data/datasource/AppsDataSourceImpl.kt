package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.withContext

class AppsDataSourceImpl(
    private val dispatcherProvider: DispatcherProvider,
) : AppsDataSource {

    // ── Package list ──────────────────────────────────────────────────────────

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
        val output = ProcessBuilder(args).redirectErrorStream(true).start().let { p ->
            val text = p.inputStream.bufferedReader().readText()
            p.waitFor()
            text
        }
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

    // ── App details ───────────────────────────────────────────────────────────

    override suspend fun getAppDetails(deviceSerial: String, packageName: String): AppDetails = withContext(dispatcherProvider.io) {
        val dump = runShell(deviceSerial, "dumpsys package $packageName")

        // "    versionCode=123 minSdk=21 targetSdk=33"
        val sdkLine = dump.lines().firstOrNull { "versionCode=" in it && "minSdk=" in it }
        val versionCode = sdkLine?.extractLong("versionCode=")
        val minSdk = sdkLine?.extractInt("minSdk=")
        val targetSdk = sdkLine?.extractInt("targetSdk=")

        val versionName = dump.lines().firstOrNull { it.trim().startsWith("versionName=") }
            ?.trim()?.removePrefix("versionName=")?.takeIf { it.isNotBlank() }

        val firstInstall = dump.lines().firstOrNull { it.trim().startsWith("firstInstallTime=") }
            ?.trim()?.removePrefix("firstInstallTime=")?.takeIf { it.isNotBlank() }

        val lastUpdate = dump.lines().firstOrNull { it.trim().startsWith("lastUpdateTime=") }
            ?.trim()?.removePrefix("lastUpdateTime=")?.takeIf { it.isNotBlank() }

        val codeSize = runCodeSize(deviceSerial, packageName)
        val (dataSize, cacheSize) = runAppSizes(deviceSerial, packageName)

        AppDetails(
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            targetSdk = targetSdk,
            minSdk = minSdk,
            firstInstallTime = firstInstall,
            lastUpdateTime = lastUpdate,
            codeSize = codeSize,
            dataSize = dataSize,
            cacheSize = cacheSize,
        )
    }

    private fun runCodeSize(deviceSerial: String, packageName: String): Long? {
        val pathOutput = runShell(deviceSerial, "pm path $packageName")
        // Sum sizes of all APK paths (handles split APKs)
        return pathOutput.lines()
            .map { it.trim().removePrefix(PACKAGE_PREFIX) }
            .filter { it.startsWith("/") }
            .mapNotNull { path ->
                runShell(deviceSerial, "stat -c %s $path").trim().toLongOrNull()
            }
            .takeIf { it.isNotEmpty() }
            ?.sum()
    }

    private fun runAppSizes(deviceSerial: String, packageName: String): Pair<Long?, Long?> {
        val output = runShell(deviceSerial, "cmd package get-app-sizes $packageName")
        val data = output.lines().firstOrNull { "Data size:" in it }
            ?.substringAfter("Data size:")?.trim()?.toLongOrNull()
        val cache = output.lines().firstOrNull { "Cache size:" in it }
            ?.substringAfter("Cache size:")?.trim()?.toLongOrNull()
        return data to cache
    }

    // ── App actions ───────────────────────────────────────────────────────────

    override suspend fun executeAction(deviceSerial: String, packageName: String, action: AppAction): Unit = withContext(dispatcherProvider.io) {
        when (action) {
            AppAction.OPEN -> runShell(deviceSerial, "monkey -p $packageName -c android.intent.category.LAUNCHER 1")
            AppAction.FORCE_STOP -> runShellChecked(deviceSerial, "am force-stop $packageName")
            AppAction.CLEAR_CACHE -> runShellChecked(deviceSerial, "pm clear --cache-only $packageName")
            AppAction.CLEAR_DATA -> runShellChecked(deviceSerial, "pm clear $packageName")
            AppAction.UNINSTALL -> runShellChecked(deviceSerial, "pm uninstall $packageName")
        }
    }

    // ── ADB shell helpers ─────────────────────────────────────────────────────

    private fun runShell(deviceSerial: String, command: String): String {
        val process = ProcessBuilder("adb", "-s", deviceSerial, "shell", command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.trim()
    }

    /** Like [runShell] but throws if the output signals a well-known pm/am failure. */
    private fun runShellChecked(deviceSerial: String, command: String): String {
        val output = runShell(deviceSerial, command)
        if (output.startsWith("Failure") || output.startsWith("Error") || output.startsWith("Exception")) {
            throw RuntimeException(output)
        }
        return output
    }

    private fun String.extractInt(prefix: String): Int? {
        val start = indexOf(prefix).takeIf { it >= 0 } ?: return null
        return substring(start + prefix.length).takeWhile { it.isDigit() }.toIntOrNull()
    }

    private fun String.extractLong(prefix: String): Long? {
        val start = indexOf(prefix).takeIf { it >= 0 } ?: return null
        return substring(start + prefix.length).takeWhile { it.isDigit() }.toLongOrNull()
    }

    private companion object {
        const val PACKAGE_PREFIX = "package:"
    }
}
