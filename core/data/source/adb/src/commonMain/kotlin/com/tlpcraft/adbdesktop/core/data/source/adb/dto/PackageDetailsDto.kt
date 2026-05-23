package com.tlpcraft.adbdesktop.core.data.source.adb.dto

data class PackageDetailsDto(
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
