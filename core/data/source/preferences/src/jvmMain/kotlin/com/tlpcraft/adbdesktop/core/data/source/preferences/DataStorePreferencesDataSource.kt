package com.tlpcraft.adbdesktop.core.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStorePreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
    private val logger: AppLogger,
) : PreferencesDataSource {
    private val preferences: Flow<Preferences> = dataStore.data.catch { failure ->
        if (failure !is IOException) throw failure
        logger.warn(TAG, "Could not read the settings file", failure)
        emit(emptyPreferences())
    }

    override fun observeAdbPath(): Flow<String?> = observe(KEY_ADB_PATH)

    override suspend fun writeAdbPath(path: String?) {
        dataStore.edit { if (path == null) it.remove(KEY_ADB_PATH) else it[KEY_ADB_PATH] = path }
    }

    override fun observeThemeMode(): Flow<String?> = observe(KEY_THEME_MODE)

    override suspend fun writeThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    private fun observe(key: Preferences.Key<String>): Flow<String?> = preferences.map { it[key] }.distinctUntilChanged()

    private companion object {
        const val TAG = "Preferences"
        val KEY_ADB_PATH = stringPreferencesKey("adb_path")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
