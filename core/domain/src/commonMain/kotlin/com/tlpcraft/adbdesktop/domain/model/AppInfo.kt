package com.tlpcraft.adbdesktop.domain.model

/**
 * A single installed package on a connected Android device, as reported by
 * `pm list packages`.
 *
 * @property packageName The fully-qualified package identifier (e.g. `com.google.android.gms`).
 */
data class AppInfo(val packageName: String)
