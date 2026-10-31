package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Parses `pm list packages` and `pm path`, both of which prefix each entry with `package:`. */
internal object PackageListParser {
    private const val PREFIX = "package:"

    fun parse(output: String): List<String> = output.lineSequence()
        .map(String::trim)
        .filter { it.startsWith(PREFIX) }
        .map { it.removePrefix(PREFIX) }
        .filter(String::isNotEmpty)
        .toList()
}
