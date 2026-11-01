package com.tlpcraft.adbdesktop.feature.logcat.di

import com.tlpcraft.adbdesktop.feature.logcat.LogcatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val logcatModule = module {
    viewModelOf(::LogcatViewModel)
}
