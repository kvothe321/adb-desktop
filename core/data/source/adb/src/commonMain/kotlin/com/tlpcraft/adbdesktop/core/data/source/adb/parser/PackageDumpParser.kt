package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Fields read from `dumpsys package <name>`. */
internal data class PackageDump(
    val versionName: String?,
    val versionCode: Long?,
    val minSdk: Int?,
    val targetSdk: Int?,
    val firstInstallTime: String?,
    val lastUpdateTime: String?,
)

internal object PackageDumpParser {
    fun parse(output: String): PackageDump {
        val lines = output.lines().map(String::trim)
        val versionLine = lines.firstOrNull { it.startsWith("versionCode=") }
        return PackageDump(
            versionName = lines.valueOf("versionName="),
            versionCode = versionLine?.numberAfter("versionCode=")?.toLongOrNull(),
            minSdk = versionLine?.numberAfter("minSdk=")?.toIntOrNull(),
            targetSdk = versionLine?.numberAfter("targetSdk=")?.toIntOrNull(),
            firstInstallTime = lines.valueOf("firstInstallTime="),
            lastUpdateTime = lines.valueOf("lastUpdateTime="),
        )
    }

    private fun List<String>.valueOf(key: String): String? = firstOrNull { it.startsWith(key) }
        ?.removePrefix(key)
        ?.takeIf(String::isNotBlank)

    private fun String.numberAfter(key: String): String? {
        val start = indexOf(key).takeIf { it >= 0 } ?: return null
        return substring(start + key.length).takeWhile(Char::isDigit).takeIf(String::isNotEmpty)
    }
}
