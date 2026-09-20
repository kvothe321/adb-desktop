package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageDetailsDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.InstallOutputParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.PackageDumpParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.PackageListParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.ShellFailureParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.StorageParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.shellQuoted
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

class AdbPackageDataSourceImpl(private val client: AdbClient) : AdbPackageDataSource {

    override suspend fun listPackages(serial: String, filter: PackageFilterDto): List<String> {
        val args = listOfNotNull("pm", "list", "packages", filter.flag)
        return PackageListParser.parse(client.shell(serial, args))
    }

    override suspend fun readPackageDetails(serial: String, packageName: String): PackageDetailsDto {
        val dump = PackageDumpParser.parse(client.shell(serial, "dumpsys", "package", packageName))
        val appSizes = readAppSizes(serial, packageName)
        return PackageDetailsDto(
            packageName = packageName,
            versionName = dump.versionName,
            versionCode = dump.versionCode,
            targetSdk = dump.targetSdk,
            minSdk = dump.minSdk,
            firstInstallTime = dump.firstInstallTime,
            lastUpdateTime = dump.lastUpdateTime,
            codeSizeBytes = readCodeSize(serial, packageName),
            dataSizeBytes = appSizes.dataBytes,
            cacheSizeBytes = appSizes.cacheBytes,
        )
    }

    override suspend fun runAction(serial: String, packageName: String, action: PackageActionDto) {
        val args = action.shellArgs(packageName)
        val output = withTimeoutOrNull(ACTION_TIMEOUT) { client.shell(serial, args) }
            ?: throw AdbException.CommandFailed(listOf("shell") + args, exitCode = TIMED_OUT_EXIT_CODE, output = "Timed out")
        if (action != PackageActionDto.LAUNCH && ShellFailureParser.isFailure(output)) {
            throw AdbException.CommandFailed(listOf("shell") + args, exitCode = 0, output = output.trim())
        }
    }

    override suspend fun install(serial: String, apkPaths: List<String>) {
        val command = if (apkPaths.size == 1) "install" else "install-multiple"
        // -t because debug builds from an IDE are marked test-only, and the package manager refuses them otherwise.
        val args = listOf("-s", serial, command, "-r", "-t") + apkPaths
        val result = client.execute(args)
        val output = result.stdout + result.stderr
        if (!InstallOutputParser.isSuccess(output)) {
            throw AdbException.CommandFailed(args, result.exitCode, InstallOutputParser.failureReason(output))
        }
    }

    /** Older Android versions lack this command; their details simply show no sizes. */
    private suspend fun readAppSizes(serial: String, packageName: String): StorageParser.AppSizes =
        StorageParser.parseAppSizes(client.shellResult(serial, listOf("cmd", "package", "get-package-storage-stats", packageName)).stdout)

    /** Split APKs install several files; the code size is their sum. */
    private suspend fun readCodeSize(serial: String, packageName: String): Long? {
        val apkPaths = PackageListParser.parse(client.shell(serial, "pm", "path", packageName))
        if (apkPaths.isEmpty()) return null
        return StorageParser.parseTotalSize(client.shell(serial, listOf("stat", "-c", "%s") + apkPaths))
    }

    private fun PackageActionDto.shellArgs(packageName: String): List<String> = when (this) {
        PackageActionDto.LAUNCH -> listOf("monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
        PackageActionDto.FORCE_STOP -> listOf("am", "force-stop", packageName)
        PackageActionDto.CLEAR_CACHE -> listOf("run-as", packageName, "sh", "-c", CLEAR_CACHE_SCRIPT.shellQuoted())
        PackageActionDto.CLEAR_DATA -> listOf("pm", "clear", packageName)
        PackageActionDto.UNINSTALL -> listOf("pm", "uninstall", packageName)
    }

    private companion object {
        val ACTION_TIMEOUT = 60.seconds
        const val TIMED_OUT_EXIT_CODE = -1

        /**
         * `pm clear --cache-only` needs a permission the adb shell lacks: Android ignores the request and the
         * command never returns. `run-as` works, but only for debuggable apps; others fail with adb's reason.
         */
        const val CLEAR_CACHE_SCRIPT = "rm -rf cache/* code_cache/*"
    }
}
