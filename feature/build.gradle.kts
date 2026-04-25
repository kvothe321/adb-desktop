plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.feature.devices)
                api(projects.feature.apps)
            }
        }
    }
}
