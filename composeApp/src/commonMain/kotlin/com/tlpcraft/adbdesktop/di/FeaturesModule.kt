package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.SharedDeviceViewModel
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveDevicesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val sharedModule = module {
    singleOf(::ObserveDevicesUseCase)
    viewModelOf(::SharedDeviceViewModel)
}

val featuresModule
    get() = listOf(
        sharedModule,
        devicesModule,
        appsModule,
    ).toTypedArray()
