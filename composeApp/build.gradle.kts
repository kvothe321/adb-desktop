import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig
import com.tlpcraft.adbdesktop.plugin.config.AndroidBuildConfig.TEST_INSTRUMENTATION_RUNNER
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    // Android Target is needed for the Previews to work
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.preview)
        }

        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":core:domain"))
            implementation(project(":adbdesktop-ui-kit"))
            implementation(projects.feature)

            // In sync with ComposeMultiplatformLibrary convention plugin
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.preview)
            implementation(libs.material.icons.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewModel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.compose.uiTooling)
        }
    }
}

android {
    namespace = AndroidBuildConfig.ROOT_NAMESPACE
    compileSdk = AndroidBuildConfig.COMPILE_SDK

    defaultConfig {
        applicationId = AndroidBuildConfig.ROOT_NAMESPACE
        minSdk = AndroidBuildConfig.MIN_SDK
        targetSdk = AndroidBuildConfig.TARGET_SDK
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER

        testOptions {
            compileSdk = AndroidBuildConfig.COMPILE_SDK
            minSdk = AndroidBuildConfig.TEST_MIN_SDK
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    debugImplementation(libs.androidx.ui.test.manifest)
}

compose.desktop {
    application {
        mainClass = "com.tlpcraft.adbdesktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.tlpcraft.adbdesktop"
            packageVersion = "1.0.0"
        }
    }
}
