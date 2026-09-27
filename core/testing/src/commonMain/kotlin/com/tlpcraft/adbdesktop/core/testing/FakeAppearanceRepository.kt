package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppearanceRepository : AppearanceRepository {
    val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val mirrorPinned = MutableStateFlow(false)

    override fun observeThemeMode(): Flow<ThemeMode> = themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    override fun observeMirrorPinned(): Flow<Boolean> = mirrorPinned

    override suspend fun setMirrorPinned(isPinned: Boolean) {
        mirrorPinned.value = isPinned
    }
}
