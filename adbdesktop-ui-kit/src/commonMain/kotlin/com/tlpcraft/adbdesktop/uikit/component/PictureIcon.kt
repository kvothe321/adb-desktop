package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * An icon drawn from encoded image bytes, such as an app's own icon, at the sizes of [Icon]. When [image] is `null` or
 * cannot be decoded, [fallback] is shown instead. `modifier` is for layout only.
 */
@Composable
fun PictureIcon(
    image: ByteArray?,
    fallback: IconName,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.Medium,
    fallbackTone: Tone = Tone.Neutral,
) {
    val bitmap = remember(image) { image?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() } }
    if (bitmap == null) {
        Icon(fallback, contentDescription, modifier, size, fallbackTone)
    } else {
        Image(bitmap, contentDescription, modifier.size(size.dp))
    }
}

@UiKitPreview
@Composable
private fun PictureIconFallbackPreview() = PreviewContext {
    PictureIcon(image = null, fallback = IconName.Package, contentDescription = null, size = IconSize.Hero)
}
