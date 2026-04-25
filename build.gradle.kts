plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    // region Build-logic defined plugins
    alias(libs.plugins.adbdesktop.kotlin.multiplatform.library) apply false
    alias(libs.plugins.adbdesktop.compose.multiplatform.library) apply false
    // Apply plugin to the root project too (without the need of declaring its version first, the version is "unspecified" anyway)
    alias(libs.plugins.adbdesktop.global.ktlint)
    alias(libs.plugins.adbdesktop.global.detekt)
    alias(libs.plugins.adbdesktop.commitlint)
    // endregion
}
