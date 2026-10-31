package com.tlpcraft.adbdesktop.core.data.source.preferences

import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import java.util.prefs.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Stores settings in the per-user Java preferences store (registry on Windows, plist on macOS, XML on Linux). */
class JvmPreferencesDataSource(
    private val preferences: Preferences,
    private val dispatchers: DispatcherProvider,
) : PreferencesDataSource {
    private val adbPath = MutableStateFlow(preferences.get(KEY_ADB_PATH, null))
    private val themeMode = MutableStateFlow(preferences.get(KEY_THEME_MODE, null))

    override fun observeAdbPath(): Flow<String?> = adbPath.asStateFlow()

    override suspend fun readAdbPath(): String? = adbPath.value

    override suspend fun writeAdbPath(path: String?) {
        withContext(dispatchers.io) {
            if (path == null) preferences.remove(KEY_ADB_PATH) else preferences.put(KEY_ADB_PATH, path)
            preferences.flush()
        }
        adbPath.value = path
    }

    override fun observeThemeMode(): Flow<String?> = themeMode.asStateFlow()

    override suspend fun writeThemeMode(mode: String) {
        withContext(dispatchers.io) {
            preferences.put(KEY_THEME_MODE, mode)
            preferences.flush()
        }
        themeMode.value = mode
    }

    private companion object {
        const val KEY_ADB_PATH = "adb_path"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
