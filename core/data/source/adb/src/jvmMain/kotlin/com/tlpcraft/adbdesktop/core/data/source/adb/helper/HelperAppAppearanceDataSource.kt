package com.tlpcraft.adbdesktop.core.data.source.adb.helper

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppAppearanceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.AppAppearanceDto
import java.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull

/** Reads names and icons with the device helper, which asks Android's PackageManager and draws each icon to a PNG. */
class HelperAppAppearanceDataSource(private val helper: DeviceHelper) : AdbAppAppearanceDataSource {

    override fun streamAppearances(serial: String, packageNames: List<String>): Flow<AppAppearanceDto> {
        val valid = packageNames.filter { it.matches(PACKAGE_NAME) }
        if (valid.isEmpty()) return emptyFlow()
        val runs = valid.chunked(PACKAGES_PER_RUN).map { chunk -> listOf(ICON_SIZE.toString()) + chunk }
        return helper.run(serial, MAIN_CLASS, runs).mapNotNull(::parseLine)
    }

    private fun parseLine(line: String): AppAppearanceDto? {
        val fields = line.split(' ')
        if (fields.size != FIELDS || !fields[0].matches(PACKAGE_NAME)) return null
        return AppAppearanceDto(packageName = fields[0], icon = fields[1].decoded(), label = fields[2].decoded()?.decodeToString())
    }

    private fun String.decoded(): ByteArray? = takeUnless { it == NONE }?.let { runCatching { Base64.getDecoder().decode(it) }.getOrNull() }

    private companion object {
        const val MAIN_CLASS = "AppAppearances"
        const val NONE = "-"
        const val FIELDS = 3

        /** Big enough for the largest place an icon is shown, on a screen with twice the density. */
        const val ICON_SIZE = 96

        /** Keeps the command line well under the shell's limit. */
        const val PACKAGES_PER_RUN = 100
        val PACKAGE_NAME = Regex("[A-Za-z0-9_.]+")
    }
}
