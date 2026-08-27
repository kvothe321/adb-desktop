import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.logger)
    implementation(projects.core.domain)
    implementation(projects.core.data.repository)
    implementation(projects.core.data.source.adb)
    implementation(projects.core.data.source.preferences)
    implementation(projects.adbdesktopUiKit)
    implementation(projects.feature.devices)
    implementation(projects.feature.apps)
    implementation(projects.feature.files)
    implementation(projects.feature.logcat)
    implementation(projects.feature.shell)
    implementation(projects.feature.screen)
    implementation(projects.feature.settings)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewModel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.test {
    useJUnitPlatform()
}

compose.resources {
    packageOfResClass = "com.tlpcraft.adbdesktop.resources"
}

compose.desktop {
    application {
        mainClass = "com.tlpcraft.adbdesktop.MainKt"
        // Run and package with the toolchain JDK instead of whatever JAVA_HOME points to.
        javaHome = javaToolchains
            .launcherFor { languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt()) }
            .get()
            .metadata
            .installationPath
            .asFile
            .absolutePath

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // jlink only bundles what is listed here; the settings store needs sun.misc.Unsafe at runtime.
            modules("jdk.unsupported")
            packageName = "ADB Desktop"
            packageVersion = providers.gradleProperty("appVersion").getOrElse("1.0.0")

            linux {
                // Debian package names must be lowercase without spaces.
                packageName = "adb-desktop"
                shortcut = true
            }
            macOS {
                bundleID = "com.tlpcraft.adbdesktop"
            }
            windows {
                menu = true
                shortcut = true
                // Fixed so that newer installers upgrade an existing installation.
                upgradeUuid = "08e86cef-7455-46d1-be02-923919d8ab5f"
            }
        }
    }
}
