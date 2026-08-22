plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
    id(libs.plugins.adbdesktop.compose.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.compose.material3)
            implementation(libs.material.icons.core)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    packageOfResClass = "com.tlpcraft.adbdesktop.uikit.resources"
}
