package com.tlpcraft.adbdesktop.feature.settings.di

import com.tlpcraft.adbdesktop.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}
