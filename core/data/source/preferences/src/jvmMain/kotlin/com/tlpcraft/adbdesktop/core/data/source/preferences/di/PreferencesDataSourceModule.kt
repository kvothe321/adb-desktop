package com.tlpcraft.adbdesktop.core.data.source.preferences.di

import com.tlpcraft.adbdesktop.core.data.source.preferences.JvmPreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import java.util.prefs.Preferences
import org.koin.dsl.module

private const val PREFERENCES_NODE = "com/tlpcraft/adbdesktop"

val preferencesDataSourceModule = module {
    single<PreferencesDataSource> {
        JvmPreferencesDataSource(Preferences.userRoot().node(PREFERENCES_NODE), dispatchers = get())
    }
}
