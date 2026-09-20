package com.tlpcraft.adbdesktop.core.data.source.adb.parser

internal object StorageParser {
    /** Sums `stat -c %s` output, one size per line. */
    fun parseTotalSize(output: String): Long? = output.lineSequence()
        .mapNotNull { it.trim().toLongOrNull() }
        .toList()
        .takeIf(List<Long>::isNotEmpty)
        ?.sum()

    /** Reads rows such as `data: 958464 bytes (936.00 Kb)` from `cmd package get-package-storage-stats`. */
    fun parseAppSizes(output: String): AppSizes {
        val lines = output.lines().map(String::trim)
        return AppSizes(
            dataBytes = lines.sizeAfter("data:"),
            cacheBytes = lines.sizeAfter("cache:"),
        )
    }

    private fun List<String>.sizeAfter(key: String): Long? = firstOrNull { it.startsWith(key) }
        ?.removePrefix(key)
        ?.trim()
        ?.substringBefore(' ')
        ?.toLongOrNull()

    data class AppSizes(val dataBytes: Long?, val cacheBytes: Long?)
}
