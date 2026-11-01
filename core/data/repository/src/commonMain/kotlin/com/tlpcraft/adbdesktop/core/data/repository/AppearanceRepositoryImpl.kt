package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppearanceRepositoryImpl(private val preferencesDataSource: PreferencesDataSource) : AppearanceRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = preferencesDataSource.observeThemeMode()
        .map { stored -> ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM }

    override suspend fun setThemeMode(mode: ThemeMode) = preferencesDataSource.writeThemeMode(mode.name)

    override fun observeMirrorPinned(): Flow<Boolean> = preferencesDataSource.observeMirrorPinned().map { it ?: false }

    override suspend fun setMirrorPinned(isPinned: Boolean) = preferencesDataSource.writeMirrorPinned(isPinned)
}
