package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** One row of `ls -la`. [permissions] starts with the type letter; fields are `null` when the entry is unreadable. */
data class RemoteFileDto(
    val name: String,
    val permissions: String,
    val sizeBytes: Long?,
    val modified: String?,
    val linkTarget: String? = null,
)
