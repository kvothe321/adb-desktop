package com.tlpcraft.adbdesktop.plugin.convention

import com.android.build.api.dsl.LibraryExtension
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.COMPILE_SDK
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.MIN_SDK
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.ROOT_NAMESPACE
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.TARGET_SDK
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.TEST_INSTRUMENTATION_RUNNER
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.TEST_MIN_SDK
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.ANDROID_LIBRARY
import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.KOTLIN_MULTIPLATFORM
import com.tlpcraft.adbdesktop.plugin.shared.applyPlugins
import com.tlpcraft.adbdesktop.plugin.shared.getPackageLikeProjectPathName
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

class KotlinMultiplatformLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            println("[BUILD-LOGIC][${this.path}] - Applying Kotlin Multiplatform Library Convention Plugin")
            applyPlugins(
                KOTLIN_MULTIPLATFORM,
                ANDROID_LIBRARY
            )

            addDefaultDependencies()
            configurePlatformTargets()
            configureAndroid()
        }
    }

    private fun Project.configurePlatformTargets() {
        val kotlinMultiplatformExtension = extensions.getByType<KotlinMultiplatformExtension>()
        kotlinMultiplatformExtension.apply {
            compilerOptions {
                freeCompilerArgs.addAll("-Xexpect-actual-classes")
            }

            configureAndroidTarget()
            configureDesktopTarget()
        }
    }

    private fun KotlinMultiplatformExtension.configureAndroidTarget() {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        }
    }

    private fun KotlinMultiplatformExtension.configureDesktopTarget() {
        jvm("desktop")
    }

    private fun Project.configureAndroid() {
        extensions.getByType<LibraryExtension>().apply {
            namespace = "$ROOT_NAMESPACE.${getPackageLikeProjectPathName()}"
            compileSdk = COMPILE_SDK

            defaultConfig {
                minSdk = MIN_SDK
                testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER

                testOptions {
                    compileSdk = COMPILE_SDK
                    targetSdk = TARGET_SDK
                    minSdk = TEST_MIN_SDK
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }

    /**
     *  This is an intended direct dependency on Kotlin Coroutines.
     *  I agree, having an abstraction layer built over coroutines would be much safer, but also very time-consuming.
     */
    private fun Project.addDefaultDependencies() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets {
                commonMain.dependencies {
                    val kotlinCoroutines = libs
                        .findLibrary("kotlinx-coroutines-core")
                        .orElseThrow { IllegalStateException("Library alias 'kotlinx-coroutines-core' not found in libs.toml") }

                    implementation(kotlinCoroutines)
                }
            }
        }
    }
}
