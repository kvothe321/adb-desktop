package com.tlpcraft.adbdesktop.plugin.shared

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).orElseThrow {
    IllegalStateException("Library alias '$alias' not found in libs.toml")
}

internal val VersionCatalog.library: LibraryAccessor
    get() = LibraryAccessor(this)

class LibraryAccessor(private val catalog: VersionCatalog) {
    val androidx: AndroidxLibraries = AndroidxLibraries(catalog)
    val koin: KoinLibraries = KoinLibraries(catalog)
    val material: MaterialLibraries = MaterialLibraries(catalog)
    val compose: ComposeLibraries = ComposeLibraries(catalog)

    // Direct access for non-nested libraries
    operator fun get(name: String): Provider<MinimalExternalModuleDependency> = catalog.library(name)
}

class ComposeLibraries(private val catalog: VersionCatalog) {
    val uiTooling: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-uiTooling")

    val material3: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-material3")

    val components: ComposeComponentsLibraries = ComposeComponentsLibraries(catalog)

    val foundation: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-foundation")

    val runtime: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-runtime")

    val ui: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-ui")

    val preview: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-preview")

    val uiTest: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-uiTest")
}

class ComposeComponentsLibraries(private val catalog: VersionCatalog) {
    val resources: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("compose-components-resources")
}

class AndroidxLibraries(private val catalog: VersionCatalog) {
    val lifecycle: LifecycleLibraries = LifecycleLibraries(catalog)
    val uiTestJunit4Android: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("androidx-ui-test-junit4-android")
    val uiTestManifest: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("androidx-ui-test-manifest")
}

class LifecycleLibraries(private val catalog: VersionCatalog) {
    val viewmodel: ViewModelLibraries = ViewModelLibraries(catalog)
    val runtime: RuntimeLibraries = RuntimeLibraries(catalog)
}

class ViewModelLibraries(private val catalog: VersionCatalog) {
    val compose: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("androidx-lifecycle-viewmodel-compose")
}

class RuntimeLibraries(private val catalog: VersionCatalog) {
    val compose: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("androidx-lifecycle-runtime-compose")
}

class KoinLibraries(private val catalog: VersionCatalog) {
    val core: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("koin-core")
    val compose: KoinComposeLibraries = KoinComposeLibraries(catalog)
}

class KoinComposeLibraries(private val catalog: VersionCatalog) {
    val core: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("koin-compose")
    val viewModel: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("koin-compose-viewModel")
}

class MaterialLibraries(catalog: VersionCatalog) {
    val icons: IconsLibraries = IconsLibraries(catalog)
}

class IconsLibraries(private val catalog: VersionCatalog) {
    val core: Provider<MinimalExternalModuleDependency>
        get() = catalog.library("material-icons-core")
}
