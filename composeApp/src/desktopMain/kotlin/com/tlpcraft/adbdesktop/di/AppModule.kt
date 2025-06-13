package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.data.di.dataModule
import com.tlpcraft.adbdesktop.feature.devices.di.devicesModule

val appModules = listOf(
    dataModule,
    devicesModule
)
