package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** Maps to the `pm list packages` flag of the same meaning. */
enum class PackageFilterDto(val flag: String?) {
    ALL(null),
    THIRD_PARTY("-3"),
    SYSTEM("-s"),
    DISABLED("-d"),
}
