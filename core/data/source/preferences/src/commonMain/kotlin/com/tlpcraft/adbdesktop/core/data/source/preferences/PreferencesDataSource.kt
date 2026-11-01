package com.tlpcraft.adbdesktop.core.data.source.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    fun observeAdbPath(): Flow<String?>

    suspend fun writeAdbPath(path: String?)

    fun observeThemeMode(): Flow<String?>

    suspend fun writeThemeMode(mode: String)

    fun observeMirrorPinned(): Flow<Boolean?>

    suspend fun writeMirrorPinned(isPinned: Boolean)
}
