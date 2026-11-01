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

    override fun observePhonePanelShown(): Flow<Boolean> = preferencesDataSource.observePhonePanelShown().map { it ?: true }

    override suspend fun setPhonePanelShown(isShown: Boolean) = preferencesDataSource.writePhonePanelShown(isShown)
}
