package com.tlpcraft.adbdesktop.core.data.source.adb.helper

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppIconDataSource
import java.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull

/** Reads icons with the device helper, which asks Android's PackageManager and draws each icon to a PNG. */
class HelperAppIconDataSource(private val helper: DeviceHelper) : AdbAppIconDataSource {

    override fun streamIcons(serial: String, packageNames: List<String>): Flow<Pair<String, ByteArray?>> {
        val valid = packageNames.filter { it.matches(PACKAGE_NAME) }
        if (valid.isEmpty()) return emptyFlow()
        val runs = valid.chunked(PACKAGES_PER_RUN).map { chunk -> listOf(ICON_SIZE.toString()) + chunk }
        return helper.run(serial, MAIN_CLASS, runs).mapNotNull(::parseLine)
    }

    private fun parseLine(line: String): Pair<String, ByteArray?>? {
        val packageName = line.substringBefore(' ')
        val icon = line.substringAfter(' ', missingDelimiterValue = "")
        if (!packageName.matches(PACKAGE_NAME) || icon.isEmpty()) return null
        return packageName to icon.takeUnless { it == NO_ICON }?.let { runCatching { Base64.getDecoder().decode(it) }.getOrNull() }
    }

    private companion object {
        const val MAIN_CLASS = "AppIcons"
        const val NO_ICON = "-"

        /** Big enough for the largest place an icon is shown, on a screen with twice the density. */
        const val ICON_SIZE = 96

        /** Keeps the command line well under the shell's limit. */
        const val PACKAGES_PER_RUN = 100
        val PACKAGE_NAME = Regex("[A-Za-z0-9_.]+")
    }
}
