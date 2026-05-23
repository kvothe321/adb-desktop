package com.tlpcraft.adbdesktop.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AdbSettingsRepository {
    /** User-chosen adb executable, or `null` to detect it automatically. */
    fun observeAdbPathOverride(): Flow<String?>

    suspend fun setAdbPathOverride(path: String?)
}
