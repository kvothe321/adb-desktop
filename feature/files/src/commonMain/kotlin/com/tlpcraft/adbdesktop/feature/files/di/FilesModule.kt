package com.tlpcraft.adbdesktop.feature.files.di

import com.tlpcraft.adbdesktop.feature.files.FilesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val filesModule = module {
    viewModelOf(::FilesViewModel)
}
