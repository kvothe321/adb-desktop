package com.tlpcraft.adbdesktop.core.data.source.preferences.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.source.preferences.DataStorePreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.settingsDirectory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

private const val SETTINGS_FILE = "settings.preferences_pb"

val preferencesDataSourceModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = CoroutineScope(get<DispatcherProvider>().io + SupervisorJob()),
            produceFile = { File(settingsDirectory(), SETTINGS_FILE) },
        )
    }
    single<PreferencesDataSource> { DataStorePreferencesDataSource(dataStore = get(), logger = get()) }
}
