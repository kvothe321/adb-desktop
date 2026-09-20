package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MdnsServiceDto

/** Parses the tab separated rows of `adb mdns services`, which repeats a service once per network interface. */
internal object MdnsServicesParser {
    private const val COLUMNS = 3

    fun parse(output: String): List<MdnsServiceDto> = output.lineSequence()
        .map { it.trim().split('\t') }
        .filter { it.size == COLUMNS }
        .map { (name, type, address) -> MdnsServiceDto(name, type, address) }
        .distinct()
        .toList()
}
