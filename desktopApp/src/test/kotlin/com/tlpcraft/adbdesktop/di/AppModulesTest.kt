package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.data.source.adb.process.BinaryCommandResult
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandResult
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.feature.apps.AppsViewModel
import com.tlpcraft.adbdesktop.feature.devices.DevicesViewModel
import com.tlpcraft.adbdesktop.feature.files.FilesViewModel
import com.tlpcraft.adbdesktop.feature.logcat.LogcatViewModel
import com.tlpcraft.adbdesktop.feature.screen.ScreenViewModel
import com.tlpcraft.adbdesktop.feature.settings.SettingsViewModel
import com.tlpcraft.adbdesktop.feature.shell.ShellViewModel
import com.tlpcraft.adbdesktop.shell.AppShellViewModel
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module

private object SilentExecutor : CommandExecutor {
    override suspend fun execute(command: List<String>) = CommandResult(exitCode = 0, stdout = "", stderr = "")

    override suspend fun executeBinary(command: List<String>) = BinaryCommandResult(exitCode = 0, stdout = ByteArray(0), stderr = "")

    override fun stream(command: List<String>): Flow<String> = emptyFlow()

    override fun interact(command: List<String>, input: Flow<String>): Flow<String> = emptyFlow()
}

/** Resolves the real graph, so a missing binding fails here instead of when a screen first opens. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    test("every screen's ViewModel can be created") {
        val koin = koinApplication {
            modules(appModules + module { single<CommandExecutor> { SilentExecutor } })
        }.koin

        koin.get<HostFiles>()
        koin.get<HostClipboard>()
        koin.get<AppShellViewModel>()
        koin.get<DevicesViewModel>()
        koin.get<AppsViewModel>()
        koin.get<FilesViewModel>()
        koin.get<LogcatViewModel>()
        koin.get<ShellViewModel>()
        koin.get<ScreenViewModel>()
        koin.get<SettingsViewModel>()
    }
})
