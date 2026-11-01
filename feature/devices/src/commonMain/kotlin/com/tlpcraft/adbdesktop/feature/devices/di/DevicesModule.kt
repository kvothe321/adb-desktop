package com.tlpcraft.adbdesktop.feature.devices.di

import com.tlpcraft.adbdesktop.feature.devices.DevicesViewModel
import com.tlpcraft.adbdesktop.feature.devices.QuickActionRunner
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val devicesModule = module {
    viewModelOf(::DevicesViewModel)
    factoryOf(::QuickActionRunner)
}
