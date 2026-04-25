package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.data.di.dataModule

val coreModule
    get() = listOf(
        dataModule
    ).toTypedArray()
