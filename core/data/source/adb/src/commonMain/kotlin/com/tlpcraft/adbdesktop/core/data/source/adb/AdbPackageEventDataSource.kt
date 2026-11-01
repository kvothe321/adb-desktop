package com.tlpcraft.adbdesktop.core.data.source.adb

import kotlinx.coroutines.flow.Flow

interface AdbPackageEventDataSource {
    /**
     * Emits the name of each package installed, removed or updated on the device, until cancelled. It ends or fails
     * when the device cannot report changes.
     */
    fun streamPackageChanges(serial: String): Flow<String>
}
