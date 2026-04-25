package com.tlpcraft.adbdesktop.plugin.convention

import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.COMPOSE_COMPILER
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.COMPOSE_MULTIPLATFORM
import com.tlpcraft.adbdesktop.plugin.shared.applyPlugins
import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            println("[BUILD-LOGIC][${this.path}] - Applying Compose Multiplatform Library Convention Plugin")
            applyPlugins(
                COMPOSE_MULTIPLATFORM,
                COMPOSE_COMPILER
            )

            declareDefaultDependencies()
        }
    }

    private fun Project.declareDefaultDependencies() {
        dependencies {
            add("debugImplementation", libs.library.compose.uiTooling)

            // Experimental Common Compose Test
            add("androidTestImplementation", libs.library.androidx.uiTestJunit4Android)
            add("debugImplementation", libs.library.androidx.uiTestManifest)
        }

        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.library.compose.runtime)
                    implementation(libs.library.compose.foundation)
                    implementation(libs.library.compose.material3)
                    implementation(libs.library.compose.ui)
                    implementation(libs.library.compose.components.resources)
                    implementation(libs.library.compose.preview)
                    implementation(libs.library.material.icons.core)
                    implementation(libs.library.koin.core)
                    implementation(libs.library.koin.compose.core)
                    implementation(libs.library.koin.compose.viewModel)
                    implementation(libs.library.androidx.lifecycle.runtime.compose)
                    implementation(libs.library.androidx.lifecycle.viewmodel.compose)
                }

                commonTest.dependencies {
                    implementation(libs.library.compose.uiTest)
                }
            }
        }
    }
}
