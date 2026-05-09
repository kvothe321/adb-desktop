package com.tlpcraft.adbdesktop.domain.model

/**
 * Extended metadata for a single installed package, fetched on demand when a package is selected.
 * All fields are nullable — any may be unavailable depending on Android version or device permissions.
 *
 * @property codeSize APK size(s) in bytes, derived from `pm path` + `stat`.
 * @property dataSize App data directory size in bytes, from `cmd package get-app-sizes` (Android 9+).
 * @property cacheSize Cache directory size in bytes, from `cmd package get-app-sizes` (Android 9+).
 */
data class AppDetails(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long?,
    val targetSdk: Int?,
    val minSdk: Int?,
    val firstInstallTime: String?,
    val lastUpdateTime: String?,
    val codeSize: Long?,
    val dataSize: Long?,
    val cacheSize: Long?,
)
