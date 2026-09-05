package com.tlpcraft.adbdesktop.plugin.convention

import com.tlpcraft.adbdesktop.plugin.shared.PluginDefinitions.KOTLIN_MULTIPLATFORM
import com.tlpcraft.adbdesktop.plugin.shared.applyPlugins
import com.tlpcraft.adbdesktop.plugin.shared.javaToolchainVersion
import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base convention for every shared module: a Kotlin Multiplatform library with a single JVM target.
 *
 * The app ships on desktop only, but shared modules stay multiplatform so that pure code (domain,
 * presentation, design system) can't reach for JVM-only APIs by accident.
 *
 * Tests are written with Kotest, kotlinx-coroutines-test and Turbine, and run on the JUnit Platform.
 * `-Pkotest.tags=<expression>` limits a run to tagged specs, for example `platform` for host-specific code.
 */
class KotlinMultiplatformLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyPlugins(KOTLIN_MULTIPLATFORM)

            extensions.getByType<KotlinMultiplatformExtension>().apply {
                jvmToolchain(javaToolchainVersion)
                compilerOptions { allWarningsAsErrors.set(true) }
                jvm()

                sourceSets {
                    commonMain.dependencies {
                        // Coroutines are an intended direct dependency of every module: Flow is part of the domain contracts.
                        implementation(libs.library("kotlinx-coroutines-core"))
                    }
                    commonTest.dependencies {
                        implementation(libs.library("kotest-framework-engine"))
                        implementation(libs.library("kotest-assertions-core"))
                        implementation(libs.library("kotlinx-coroutines-test"))
                        implementation(libs.library("turbine"))
                    }
                    jvmTest.dependencies {
                        implementation(libs.library("kotest-runner-junit5"))
                    }
                }
            }

            val kotestTags = providers.gradleProperty(KOTEST_TAGS).orNull
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
                kotestTags?.let { systemProperty(KOTEST_TAGS, it) }
            }
        }
    }

    private companion object {
        const val KOTEST_TAGS = "kotest.tags"
    }
}
