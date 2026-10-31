package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto

/**
 * Parses the long device listing shared by `adb devices -l` and `adb track-devices -l`:
 * `<serial> <state> [usb:<path>] [product:<x>] [model:<x>] [device:<x>] transport_id:<n>`.
 */
internal object DeviceListParser {
    private const val HEADER = "List of devices attached"
    private const val NO_PERMISSIONS_STATE = "no permissions"
    private const val NO_PERMISSIONS_END = "]"

    fun parse(output: String): List<DeviceDto> = output.lineSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && it != HEADER && !it.startsWith("*") }
        .mapNotNull(::parseLine)
        .toList()

    private fun parseLine(line: String): DeviceDto? {
        val serial = line.substringBefore(' ').substringBefore('\t')
        val rest = line.removePrefix(serial).trim()
        if (serial.isEmpty() || rest.isEmpty()) return null

        val (state, attributesText) = splitState(rest)
        val attributes = parseAttributes(attributesText)
        return DeviceDto(
            serial = serial,
            state = state,
            usbPath = attributes["usb"],
            product = attributes["product"],
            model = attributes["model"],
            device = attributes["device"],
            transportId = attributes["transport_id"]?.toLongOrNull(),
        )
    }

    /** The `no permissions` state carries a free-text hint ending in `]`, everything else is one word. */
    private fun splitState(text: String): Pair<String, String> {
        if (text.startsWith(NO_PERMISSIONS_STATE)) {
            val hintEnd = text.indexOf(NO_PERMISSIONS_END)
            val attributes = if (hintEnd >= 0) text.substring(hintEnd + 1) else ""
            return NO_PERMISSIONS_STATE to attributes
        }
        return text.substringBefore(' ') to text.substringAfter(' ', missingDelimiterValue = "")
    }

    private fun parseAttributes(text: String): Map<String, String> = text.split(' ', '\t')
        .filter { ':' in it }
        .associate { it.substringBefore(':') to it.substringAfter(':') }
}
