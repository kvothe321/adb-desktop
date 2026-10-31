package com.tlpcraft.adbdesktop.plugin.config

/**
 * Settings for the preview-only Android target of Compose modules.
 *
 * Nothing is ever shipped for Android: the target exists because IDE `@Preview` rendering
 * for Compose Multiplatform still runs on the Android tooling.
 */
object AndroidPreviewConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val ROOT_NAMESPACE = "com.tlpcraft.adbdesktop"
}
