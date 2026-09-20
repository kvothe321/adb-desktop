package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** One row of `adb mdns services`. [type] is the raw service, e.g. `_adb-tls-pairing._tcp`. */
data class MdnsServiceDto(val name: String, val type: String, val address: String)
