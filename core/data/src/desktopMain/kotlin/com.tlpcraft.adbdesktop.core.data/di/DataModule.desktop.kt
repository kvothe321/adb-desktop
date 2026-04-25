package com.tlpcraft.adbdesktop.core.data.di

import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.datasource.AppsDataSource
import com.tlpcraft.adbdesktop.core.data.datasource.AppsDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.datasource.CpuDataSource
import com.tlpcraft.adbdesktop.core.data.datasource.CpuDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.datasource.DeviceInfoDataSource
import com.tlpcraft.adbdesktop.core.data.datasource.DeviceInfoDataSourceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformDataModule: Module? = module {
    singleOf(::AdbDeviceDataSourceImpl) bind AdbDeviceDataSource::class
    singleOf(::CpuDataSourceImpl) bind CpuDataSource::class
    singleOf(::AppsDataSourceImpl) bind AppsDataSource::class
    singleOf(::DeviceInfoDataSourceImpl) bind DeviceInfoDataSource::class
}
