package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A centered placeholder for empty, loading or failed content, with an optional call to action.
 * `modifier` is for layout only.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: IconName? = null,
    tone: Tone = Tone.Neutral,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(AppTheme.spacing.xl).widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        if (icon != null) {
            Box(
                Modifier.size(ICON_CIRCLE).background(
                    if (tone ==
                        Tone.Neutral
                    ) {
                        AppTheme.colors.surfaceRaised
                    } else {
                        tone.color.copy(alpha = TINT_ALPHA)
                    },
                    AppTheme.shapes.pill,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, size = IconSize.Large, tone = tone)
            }
            Spacer(Modifier.height(AppTheme.spacing.xs))
        }
        Text(title, variant = TextVariant.Title, tone = tone)
        if (description != null) {
            BasicText(
                text = description,
                style = AppTheme.typography.body.copy(color = AppTheme.colors.textSecondary, textAlign = TextAlign.Center),
            )
        }
        if (action != null) {
            Spacer(Modifier.height(AppTheme.spacing.xs))
            action()
        }
    }
}

private val ICON_CIRCLE = 64.dp
private const val TINT_ALPHA = 0.12f

@UiKitPreview
@Composable
private fun EmptyStatePreview() = PreviewContext {
    EmptyState(
        title = "No devices",
        description = "Plug in a device with USB debugging enabled, or connect over Wi-Fi.",
        icon = IconName.Devices,
        action = { Button(text = "Connect over Wi-Fi", onClick = {}, variant = ButtonVariant.Primary) },
    )
}
