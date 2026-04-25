package com.tlpcraft.adbdesktop.uikit.preview

import androidx.compose.ui.tooling.preview.Preview

private const val NIGHT_MODE_NO = 0x10
private const val NIGHT_MODE_YES = 0x20

@Preview(uiMode = NIGHT_MODE_NO, name = "Light", showBackground = true, backgroundColor = 0xFFE0E0E0)
@Preview(uiMode = NIGHT_MODE_YES, name = "Dark")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class UiKitPreview
