package com.tlpcraft.adbdesktop.core.data.repository.di

import com.tlpcraft.adbdesktop.core.data.repository.AdbSettingsRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.AppearanceRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.AppsRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.CpuRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.DeviceInfoRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.DeviceRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.DeviceSelectionRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.FilesRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.LogcatRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.ScreenRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.repository.ShellRepositoryImpl
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbPathOverrideProvider
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.repository.CpuRepository
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceInfoRepository
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    single { AdbPathOverrideProvider { get<PreferencesDataSource>().readAdbPath() } }
    singleOf(::DeviceRepositoryImpl) bind DeviceRepository::class
    singleOf(::DeviceSelectionRepositoryImpl) bind DeviceSelectionRepository::class
    single<DeviceInfoRepository> { DeviceInfoRepositoryImpl(get()) }
    single<CpuRepository> { CpuRepositoryImpl(get()) }
    singleOf(::AppsRepositoryImpl) bind AppsRepository::class
    singleOf(::AdbSettingsRepositoryImpl) bind AdbSettingsRepository::class
    singleOf(::AppearanceRepositoryImpl) bind AppearanceRepository::class
    single<LogcatRepository> { LogcatRepositoryImpl(get()) }
    singleOf(::ShellRepositoryImpl) bind ShellRepository::class
    single<FilesRepository> { FilesRepositoryImpl(get()) }
    singleOf(::ScreenRepositoryImpl) bind ScreenRepository::class
}
