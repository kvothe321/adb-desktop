package com.tlpcraft.adbdesktop.core.data.di

import com.tlpcraft.adbdesktop.core.data.repository.AdbDeviceRepositoryImpl
import com.tlpcraft.adbdesktop.domain.repository.AdbDeviceRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformDataModule: Module?

val dataModule = module {
    platformDataModule?.let { includes(it) }
    singleOf(::AdbDeviceRepositoryImpl) bind AdbDeviceRepository::class
}
