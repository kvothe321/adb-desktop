package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.SharedDeviceViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val shellModule = module {
    viewModelOf(::SharedDeviceViewModel)
}

val featureModules = listOf(
    shellModule,
    devicesModule,
    appsModule,
)
