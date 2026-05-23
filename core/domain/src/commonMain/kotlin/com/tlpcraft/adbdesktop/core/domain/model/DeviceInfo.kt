package com.tlpcraft.adbdesktop.core.domain.model

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val batteryLevel: Int?,
)
