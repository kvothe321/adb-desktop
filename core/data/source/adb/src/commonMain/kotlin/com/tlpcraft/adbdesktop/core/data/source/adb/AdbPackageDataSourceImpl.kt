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

class AdbPackageDataSourceImpl(private val client: AdbClient) : AdbPackageDataSource {

    override suspend fun listPackages(serial: String, filter: PackageFilterDto): List<String> {
        val args = listOfNotNull("pm", "list", "packages", filter.flag)
        return PackageListParser.parse(client.shell(serial, args))
    }

    override suspend fun readPackageDetails(serial: String, packageName: String): PackageDetailsDto {
        val dump = PackageDumpParser.parse(client.shell(serial, "dumpsys", "package", packageName))
        val appSizes = StorageParser.parseAppSizes(client.shell(serial, "cmd", "package", "get-app-sizes", packageName))
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
        val output = client.shell(serial, args)
        if (action != PackageActionDto.LAUNCH && ShellFailureParser.isFailure(output)) {
            throw AdbException.CommandFailed(listOf("shell") + args, exitCode = 0, output = output.trim())
        }
    }

    override suspend fun install(serial: String, apkPaths: List<String>) {
        val command = if (apkPaths.size == 1) "install" else "install-multiple"
        val args = listOf("-s", serial, command, "-r") + apkPaths
        val result = client.execute(args)
        val output = result.stdout + result.stderr
        if (!InstallOutputParser.isSuccess(output)) {
            throw AdbException.CommandFailed(args, result.exitCode, InstallOutputParser.failureReason(output))
        }
    }

    /** Split APKs install several files; the code size is their sum. */
    private suspend fun readCodeSize(serial: String, packageName: String): Long? {
        val apkPaths = PackageListParser.parse(client.shell(serial, "pm", "path", packageName))
        if (apkPaths.isEmpty()) return null
        return StorageParser.parseTotalSize(client.shell(serial, listOf("stat", "-c", "%s") + apkPaths))
    }

    private fun PackageActionDto.shellArgs(packageName: String): List<String> = when (this) {
        PackageActionDto.LAUNCH -> listOf("monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
        PackageActionDto.FORCE_STOP -> listOf("am", "force-stop", packageName)
        PackageActionDto.CLEAR_CACHE -> listOf("pm", "clear", "--cache-only", packageName)
        PackageActionDto.CLEAR_DATA -> listOf("pm", "clear", packageName)
        PackageActionDto.UNINSTALL -> listOf("pm", "uninstall", packageName)
    }
}
