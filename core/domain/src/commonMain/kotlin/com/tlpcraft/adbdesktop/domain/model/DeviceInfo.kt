package com.tlpcraft.adbdesktop.domain.model

/**
 * Human-readable metadata about a connected Android device.
 *
 * @property name Manufacturer + model name (e.g. "Google Pixel 7 Pro").
 * @property androidVersion OS version string (e.g. "Android 14").
 * @property batteryLevel Battery charge level 0–100, or `null` if the sysfs node is unavailable.
 */
data class DeviceInfo(
    val name: String,
    val androidVersion: String,
    val batteryLevel: Int?,
)
