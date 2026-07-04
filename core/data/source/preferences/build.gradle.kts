plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.koin.core)
        }
    }
}
