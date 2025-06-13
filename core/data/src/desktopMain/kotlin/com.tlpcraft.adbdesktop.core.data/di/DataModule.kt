package com.tlpcraft.adbdesktop.core.data.di

import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.repository.AdbDeviceRepositoryImpl
import com.tlpcraft.adbdesktop.domain.repository.AdbDeviceRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AdbDeviceDataSourceImpl) bind AdbDeviceDataSource::class
    singleOf(::AdbDeviceRepositoryImpl) bind AdbDeviceRepository::class
}
