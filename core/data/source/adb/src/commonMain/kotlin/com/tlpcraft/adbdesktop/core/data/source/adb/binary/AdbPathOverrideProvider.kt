package com.tlpcraft.adbdesktop.core.data.source.adb.binary

fun interface AdbPathOverrideProvider {
    suspend fun adbPathOverride(): String?
}
