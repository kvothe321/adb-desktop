plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
    id(libs.plugins.adbdesktop.compose.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
