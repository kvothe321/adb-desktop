package com.tlpcraft.adbdesktop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
private sealed interface Route : NavKey

@Serializable
data object DevicesRoute : Route

@Serializable
data object AppsRoute : Route

@OptIn(ExperimentalSerializationApi::class)
internal val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}
