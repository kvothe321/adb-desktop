package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** One row of `adb devices -l`. [state] is the raw adb state, e.g. `device`, `offline`, `unauthorized`. */
data class DeviceDto(
    val serial: String,
    val state: String,
    val usbPath: String? = null,
    val product: String? = null,
    val model: String? = null,
    val device: String? = null,
    val transportId: Long? = null,
)
