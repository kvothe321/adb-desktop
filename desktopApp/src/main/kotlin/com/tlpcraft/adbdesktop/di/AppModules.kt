package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.common.dispatcher.DefaultDispatcherProvider
import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.repository.di.repositoryModule
import com.tlpcraft.adbdesktop.core.data.source.adb.di.adbDataSourceModule
import com.tlpcraft.adbdesktop.core.data.source.preferences.di.preferencesDataSourceModule
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import com.tlpcraft.adbdesktop.core.logger.KermitAppLogger
import com.tlpcraft.adbdesktop.feature.apps.di.appsModule
import com.tlpcraft.adbdesktop.feature.devices.di.devicesModule
import com.tlpcraft.adbdesktop.feature.files.di.filesModule
import com.tlpcraft.adbdesktop.feature.logcat.di.logcatModule
import com.tlpcraft.adbdesktop.feature.screen.di.screenModule
import com.tlpcraft.adbdesktop.feature.settings.di.settingsModule
import com.tlpcraft.adbdesktop.feature.shell.di.shellModule
import com.tlpcraft.adbdesktop.host.JvmHostClipboard
import com.tlpcraft.adbdesktop.host.JvmHostFiles
import com.tlpcraft.adbdesktop.shell.AppShellViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val infrastructureModule = module {
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    single<AppLogger> { KermitAppLogger() }
    singleOf(::JvmHostFiles) bind HostFiles::class
    singleOf(::JvmHostClipboard) bind HostClipboard::class
}

private val appShellModule = module {
    viewModelOf(::AppShellViewModel)
}

val appModules = listOf(
    infrastructureModule,
    adbDataSourceModule,
    preferencesDataSourceModule,
    repositoryModule,
    domainModule,
    appShellModule,
    devicesModule,
    appsModule,
    filesModule,
    logcatModule,
    shellModule,
    screenModule,
    settingsModule,
)
