plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data.source.adb)
            implementation(projects.core.data.source.preferences)
            implementation(libs.koin.core)
        }
    }
}
