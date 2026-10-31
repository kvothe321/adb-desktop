package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import com.tlpcraft.adbdesktop.domain.service.DispatcherProviderImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    singleOf(::DispatcherProviderImpl) bind DispatcherProvider::class
}
