plugins {
    id(libs.plugins.adbdesktop.feature.library.get().pluginId)
}

compose.resources {
    packageOfResClass = "com.tlpcraft.adbdesktop.feature.settings.resources"
}
