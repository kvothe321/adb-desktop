plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
