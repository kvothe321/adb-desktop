package com.tlpcraft.adbdesktop.core.data.source.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    fun observeAdbPath(): Flow<String?>

    suspend fun readAdbPath(): String?

    suspend fun writeAdbPath(path: String?)
}
