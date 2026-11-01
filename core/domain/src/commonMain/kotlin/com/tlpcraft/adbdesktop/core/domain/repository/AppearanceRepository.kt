package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppearanceRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)

    /** Whether the live phone panel shows beside the sections. It does until the user hides it. */
    fun observePhonePanelShown(): Flow<Boolean>

    suspend fun setPhonePanelShown(isShown: Boolean)
}
