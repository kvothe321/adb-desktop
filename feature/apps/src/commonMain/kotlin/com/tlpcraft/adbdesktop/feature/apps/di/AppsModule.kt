package com.tlpcraft.adbdesktop.feature.apps.di

import com.tlpcraft.adbdesktop.feature.apps.AppsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appsModule = module {
    viewModelOf(::AppsViewModel)
}
