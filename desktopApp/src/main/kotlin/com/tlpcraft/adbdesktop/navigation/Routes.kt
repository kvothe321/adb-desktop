package com.tlpcraft.adbdesktop.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Route : NavKey

@Serializable
data object DevicesRoute : Route

@Serializable
data object AppsRoute : Route

@Serializable
data object FilesRoute : Route

val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DevicesRoute::class, DevicesRoute.serializer())
            subclass(AppsRoute::class, AppsRoute.serializer())
            subclass(FilesRoute::class, FilesRoute.serializer())
        }
    }
}

/** Sections are siblings, not a history: switching replaces the whole stack. */
fun NavBackStack<NavKey>.show(section: Section) {
    if (lastOrNull() == section.route) return
    clear()
    add(section.route)
}
