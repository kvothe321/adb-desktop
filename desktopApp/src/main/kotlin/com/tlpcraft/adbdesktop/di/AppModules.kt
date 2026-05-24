package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.common.dispatcher.DefaultDispatcherProvider
import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.repository.di.repositoryModule
import com.tlpcraft.adbdesktop.core.data.source.adb.di.adbDataSourceModule
import com.tlpcraft.adbdesktop.core.data.source.preferences.di.preferencesDataSourceModule
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import com.tlpcraft.adbdesktop.core.logger.KermitAppLogger
import com.tlpcraft.adbdesktop.feature.apps.di.appsModule
import com.tlpcraft.adbdesktop.feature.devices.di.devicesModule
import com.tlpcraft.adbdesktop.shell.ShellViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val infrastructureModule = module {
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    single<AppLogger> { KermitAppLogger() }
}

private val shellModule = module {
    viewModelOf(::ShellViewModel)
}

val appModules = listOf(
    infrastructureModule,
    adbDataSourceModule,
    preferencesDataSourceModule,
    repositoryModule,
    domainModule,
    shellModule,
    devicesModule,
    appsModule,
)
