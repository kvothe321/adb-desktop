package com.tlpcraft.adbdesktop.core.data.source.adb.parser

internal object StorageParser {
    /** Sums `stat -c %s` output, one size per line. */
    fun parseTotalSize(output: String): Long? = output.lineSequence()
        .mapNotNull { it.trim().toLongOrNull() }
        .toList()
        .takeIf(List<Long>::isNotEmpty)
        ?.sum()

    /** Reads `Data size:` and `Cache size:` (bytes) from `cmd package get-app-sizes`. */
    fun parseAppSizes(output: String): AppSizes {
        val lines = output.lines().map(String::trim)
        return AppSizes(
            dataBytes = lines.sizeAfter("Data size:"),
            cacheBytes = lines.sizeAfter("Cache size:"),
        )
    }

    private fun List<String>.sizeAfter(key: String): Long? = firstOrNull { it.startsWith(key) }
        ?.removePrefix(key)
        ?.trim()
        ?.toLongOrNull()

    data class AppSizes(val dataBytes: Long?, val cacheBytes: Long?)
}
