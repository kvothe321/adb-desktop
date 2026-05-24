package com.tlpcraft.adbdesktop.plugin.convention

import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.ADBDESKTOP_COMPOSE_LIBRARY
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.ADBDESKTOP_KOTLIN_LIBRARY
import com.tlpcraft.adbdesktop.plugin.shared.applyPlugins
import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** A feature sees the domain and the design system, never the data layer. */
class FeatureLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins(ADBDESKTOP_KOTLIN_LIBRARY, ADBDESKTOP_COMPOSE_LIBRARY)

            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":core:domain"))
                        implementation(project(":adbdesktop-ui-kit"))
                        implementation(libs.library.koin.core)
                        implementation(libs.library.koin.compose.core)
                        implementation(libs.library.koin.compose.viewModel)
                        implementation(libs.library.androidx.lifecycle.runtime.compose)
                        implementation(libs.library.androidx.lifecycle.viewmodel.compose)
                    }
                    commonTest.dependencies {
                        implementation(project(":core:testing"))
                    }
                }
            }
        }
    }
}
