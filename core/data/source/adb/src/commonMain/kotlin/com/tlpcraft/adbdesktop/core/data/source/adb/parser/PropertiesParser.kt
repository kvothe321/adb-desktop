package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Parses `getprop` output, one `[key]: [value]` pair per line. */
internal object PropertiesParser {
    private val propertyLine = Regex("""^\[(.+?)]: \[(.*)]$""")

    fun parse(output: String): Map<String, String> = output.lineSequence()
        .mapNotNull { propertyLine.find(it.trim()) }
        .associate { it.groupValues[1] to it.groupValues[2] }
}
