package com.tlpcraft.adbdesktop.core.domain.model

/** Every field except [packageName] may be missing, depending on Android version and permissions. */
data class AppDetails(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long?,
    val targetSdk: Int?,
    val minSdk: Int?,
    val firstInstallTime: String?,
    val lastUpdateTime: String?,
    val codeSizeBytes: Long?,
    val dataSizeBytes: Long?,
    val cacheSizeBytes: Long?,
)
