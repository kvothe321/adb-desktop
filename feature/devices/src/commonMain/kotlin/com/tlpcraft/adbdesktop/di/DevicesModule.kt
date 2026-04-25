package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.presentation.DevicesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val devicesModule = module {
    singleOf(::ObserveCpuInfoUseCase)
    viewModelOf(::DevicesViewModel)
}
