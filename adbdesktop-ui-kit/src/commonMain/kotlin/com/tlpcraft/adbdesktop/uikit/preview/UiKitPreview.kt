package com.tlpcraft.adbdesktop.uikit.preview

import androidx.compose.ui.tooling.preview.Preview

private const val UI_MODE_NIGHT_NO = 0x10
private const val UI_MODE_NIGHT_YES = 0x20

/** Renders a preview in light and dark theme. Pair it with [PreviewContext]. */
@Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class UiKitPreview
