plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.logger)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.koin.core)
        }
    }
}
