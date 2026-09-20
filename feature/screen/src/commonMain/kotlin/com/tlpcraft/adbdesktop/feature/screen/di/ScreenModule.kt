package com.tlpcraft.adbdesktop.feature.screen.di

import com.tlpcraft.adbdesktop.feature.screen.ScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val screenModule = module {
    viewModelOf(::ScreenViewModel)
}
