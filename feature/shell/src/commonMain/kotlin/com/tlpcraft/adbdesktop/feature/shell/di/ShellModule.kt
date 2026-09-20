package com.tlpcraft.adbdesktop.feature.shell.di

import com.tlpcraft.adbdesktop.feature.shell.ShellViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shellModule = module {
    viewModelOf(::ShellViewModel)
}
