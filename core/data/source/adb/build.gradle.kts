plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.logger)
        }
        jvmMain.dependencies {
            implementation(libs.koin.core)
        }
    }
}
