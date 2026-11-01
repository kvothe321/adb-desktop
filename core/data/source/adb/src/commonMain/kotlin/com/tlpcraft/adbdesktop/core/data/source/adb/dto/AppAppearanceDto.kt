package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** An app's name in the device's language and its launcher icon as PNG bytes, each `null` when it has none. */
class AppAppearanceDto(val packageName: String, val label: String?, val icon: ByteArray?)
