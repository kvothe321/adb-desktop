package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Text in one of the design system's type styles. Long text is ellipsized once [maxLines] is reached.
 * `modifier` is for layout only.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    variant: TextVariant = TextVariant.Body,
    emphasis: TextEmphasis = TextEmphasis.High,
    tone: Tone = Tone.Neutral,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = variant.style.copy(color = textColor(emphasis, tone)),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Fixed-width text for machine output such as serials, paths and logs. [selectable] lets users copy it.
 * `modifier` is for layout only.
 */
@Composable
fun MonospaceText(
    text: String,
    modifier: Modifier = Modifier,
    emphasis: TextEmphasis = TextEmphasis.High,
    tone: Tone = Tone.Neutral,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
) {
    val style = AppTheme.typography.mono.copy(color = textColor(emphasis, tone))
    if (selectable) {
        SelectionContainer(modifier) {
            BasicText(text = text, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
        }
    } else {
        BasicText(text = text, modifier = modifier, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
@ReadOnlyComposable
internal fun TextVariant.textStyle(): TextStyle = style

private val TextVariant.style: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        TextVariant.Display -> AppTheme.typography.display
        TextVariant.Title -> AppTheme.typography.title
        TextVariant.Heading -> AppTheme.typography.heading
        TextVariant.Body -> AppTheme.typography.body
        TextVariant.BodyStrong -> AppTheme.typography.bodyStrong
        TextVariant.Label -> AppTheme.typography.label
        TextVariant.Caption -> AppTheme.typography.caption
    }

@Composable
@ReadOnlyComposable
private fun textColor(emphasis: TextEmphasis, tone: Tone): Color = if (tone != Tone.Neutral) {
    tone.color
} else {
    when (emphasis) {
        TextEmphasis.High -> AppTheme.colors.textPrimary
        TextEmphasis.Medium -> AppTheme.colors.textSecondary
        TextEmphasis.Disabled -> AppTheme.colors.textDisabled
    }
}

@UiKitPreview
@Composable
private fun TextPreview() = PreviewContext {
    Column {
        TextVariant.entries.forEach { Text(it.name, variant = it) }
        Text("Secondary", emphasis = TextEmphasis.Medium)
        Text("Error", tone = Tone.Error)
        MonospaceText("emulator-5554  transport_id:1")
    }
}
