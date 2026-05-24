plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
    id(libs.plugins.adbdesktop.compose.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.material3)
            implementation(libs.material.icons.core)
        }
    }
}

compose.resources {
    packageOfResClass = "com.tlpcraft.adbdesktop.uikit.resources"
}
