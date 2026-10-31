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

    override fun observeAdbPath(): Flow<String?> = adbPath.asStateFlow()

    override suspend fun readAdbPath(): String? = adbPath.value

    override suspend fun writeAdbPath(path: String?) {
        withContext(dispatchers.io) {
            if (path == null) preferences.remove(KEY_ADB_PATH) else preferences.put(KEY_ADB_PATH, path)
            preferences.flush()
        }
        adbPath.value = path
    }

    private companion object {
        const val KEY_ADB_PATH = "adb_path"
    }
}
