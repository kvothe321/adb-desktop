package com.tlpcraft.adbdesktop.core.data.source.adb.di

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbBinaryResolver
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbEnvironment
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.JvmAdbEnvironment
import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.process.ProcessCommandExecutor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Requires an [com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbPathOverrideProvider] from the host graph. */
val adbDataSourceModule = module {
    singleOf(::JvmAdbEnvironment) bind AdbEnvironment::class
    singleOf(::ProcessCommandExecutor) bind CommandExecutor::class
    singleOf(::AdbBinaryResolver)
    singleOf(::AdbClient)
    singleOf(::AdbDeviceDataSourceImpl) bind AdbDeviceDataSource::class
    singleOf(::AdbDevicePropertiesDataSourceImpl) bind AdbDevicePropertiesDataSource::class
    singleOf(::AdbPackageDataSourceImpl) bind AdbPackageDataSource::class
}
