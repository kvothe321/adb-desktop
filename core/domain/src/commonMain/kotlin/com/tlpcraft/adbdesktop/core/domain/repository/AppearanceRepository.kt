package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppearanceRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
