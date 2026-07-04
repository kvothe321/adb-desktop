package com.tlpcraft.adbdesktop.plugin.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.tlpcraft.adbdesktop.plugin.config.AndroidPreviewConfig.COMPILE_SDK
import com.tlpcraft.adbdesktop.plugin.config.AndroidPreviewConfig.MIN_SDK
import com.tlpcraft.adbdesktop.plugin.config.AndroidPreviewConfig.ROOT_NAMESPACE
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.COMPOSE_COMPILER
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.COMPOSE_MULTIPLATFORM
import com.tlpcraft.adbdesktop.plugin.shared.applyPlugins
import com.tlpcraft.adbdesktop.plugin.shared.getPackageLikeProjectPathName
import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for modules that contain Compose UI (design system and features).
 *
 * Besides Compose itself, it adds a preview-only Android target: IDE `@Preview` rendering for
 * Compose Multiplatform runs on the Android tooling. The Android variant is never packaged.
 */
class ComposeMultiplatformLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins(
                ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY,
                COMPOSE_MULTIPLATFORM,
                COMPOSE_COMPILER,
            )

            configurePreviewAndroidTarget()
            declareDefaultDependencies()
        }
    }

    private fun Project.configurePreviewAndroidTarget() {
        extensions.getByType<KotlinMultiplatformExtension>()
            .extensions
            .getByType<KotlinMultiplatformAndroidLibraryTarget>()
            .apply {
                namespace = "$ROOT_NAMESPACE.${getPackageLikeProjectPathName()}"
                compileSdk = COMPILE_SDK
                minSdk = MIN_SDK
                androidResources { enable = true }
            }

        dependencies {
            // The Android-KMP library plugin has no build variants, so the preview tooling goes on the runtime classpath.
            add("androidRuntimeClasspath", libs.library.compose.uiTooling)
        }
    }

    private fun Project.declareDefaultDependencies() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets {
                // Material is deliberately absent: only the design system module adds it.
                commonMain.dependencies {
                    implementation(libs.library.compose.runtime)
                    implementation(libs.library.compose.foundation)
                    implementation(libs.library.compose.ui)
                    implementation(libs.library.compose.components.resources)
                    implementation(libs.library.compose.preview)
                }
            }
        }
    }
}
