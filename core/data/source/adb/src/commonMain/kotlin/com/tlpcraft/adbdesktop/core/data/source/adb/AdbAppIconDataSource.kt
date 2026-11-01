package com.tlpcraft.adbdesktop.core.data.source.adb

import kotlinx.coroutines.flow.Flow

interface AdbAppIconDataSource {
    /** Emits each package's launcher icon as PNG bytes once it is read, `null` for one without an icon. */
    fun streamIcons(serial: String, packageNames: List<String>): Flow<Pair<String, ByteArray?>>
}
