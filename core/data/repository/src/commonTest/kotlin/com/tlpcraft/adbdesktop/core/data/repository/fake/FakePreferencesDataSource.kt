package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesDataSource : PreferencesDataSource {
    private val adbPath = MutableStateFlow<String?>(null)
    val themeMode = MutableStateFlow<String?>(null)

    override fun observeAdbPath(): Flow<String?> = adbPath

    override suspend fun readAdbPath(): String? = adbPath.value

    override suspend fun writeAdbPath(path: String?) {
        adbPath.value = path
    }

    override fun observeThemeMode(): Flow<String?> = themeMode

    override suspend fun writeThemeMode(mode: String) {
        themeMode.value = mode
    }
}
