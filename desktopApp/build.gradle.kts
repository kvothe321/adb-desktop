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
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.adbdesktopUiKit)
    implementation(projects.feature.devices)
    implementation(projects.feature.apps)
    implementation(projects.feature.files)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
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
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
}

compose.resources {
    packageOfResClass = "com.tlpcraft.adbdesktop.resources"
}

compose.desktop {
    application {
        mainClass = "com.tlpcraft.adbdesktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ADB Desktop"
            packageVersion = "1.0.0"
        }
    }
}
