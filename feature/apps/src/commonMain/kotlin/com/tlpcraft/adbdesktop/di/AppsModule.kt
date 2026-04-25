package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.domain.usecase.apps.GetAppsUseCase
import com.tlpcraft.adbdesktop.presentation.AppsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appsModule = module {
    singleOf(::GetAppsUseCase)
    viewModelOf(::AppsViewModel)
}
