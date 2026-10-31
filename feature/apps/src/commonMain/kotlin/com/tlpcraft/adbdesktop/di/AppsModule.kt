package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.presentation.AppsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appsModule = module {
    viewModelOf(::AppsViewModel)
}
