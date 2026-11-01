package com.tlpcraft.adbdesktop.core.data.source.adb.di

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbFileDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbFileDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbInfoDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbInfoDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbLogcatDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbLogcatDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbMirrorDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbScreenDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbScreenDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbShellDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbShellDataSourceImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbBinaryResolver
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbEnvironment
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.JvmAdbEnvironment
import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.helper.HelperAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.mirror.ScrcpyMirrorDataSource
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
    singleOf(::AdbLogcatDataSourceImpl) bind AdbLogcatDataSource::class
    singleOf(::AdbShellDataSourceImpl) bind AdbShellDataSource::class
    singleOf(::AdbFileDataSourceImpl) bind AdbFileDataSource::class
    singleOf(::AdbScreenDataSourceImpl) bind AdbScreenDataSource::class
    singleOf(::ScrcpyMirrorDataSource) bind AdbMirrorDataSource::class
    singleOf(::HelperAppIconDataSource) bind AdbAppIconDataSource::class
    singleOf(::AdbInfoDataSourceImpl) bind AdbInfoDataSource::class
}
