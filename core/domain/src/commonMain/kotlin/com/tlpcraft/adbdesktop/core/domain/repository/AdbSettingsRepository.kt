package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import kotlinx.coroutines.flow.Flow

interface AdbSettingsRepository {
    /** User-chosen adb executable, or `null` to detect it automatically. */
    fun observeAdbPathOverride(): Flow<String?>

    suspend fun setAdbPathOverride(path: String?)

    /** Locates adb and asks it for its version. */
    suspend fun readAdbInfo(): Outcome<AdbInfo, DeviceError>
}
