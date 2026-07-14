package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Single-line text input. [error] replaces the hint below the field and colors the border.
 * Enter calls [onSubmit]; the Up and Down keys call [onArrowUp] and [onArrowDown] when set, for command
 * history. With [autoFocus] the field takes the keyboard focus when it appears. `modifier` is for layout only.
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    leadingIcon: IconName? = null,
    monospace: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    onSubmit: (() -> Unit)? = null,
    onArrowUp: (() -> Unit)? = null,
    onArrowDown: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val textStyle = if (monospace) AppTheme.typography.mono else AppTheme.typography.body
    val borderColor = if (error != null) AppTheme.colors.error else AppTheme.colors.border
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        if (label != null) Text(label, variant = TextVariant.Label, emphasis = TextEmphasis.Medium)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(color = if (enabled) AppTheme.colors.textPrimary else AppTheme.colors.textDisabled),
            cursorBrush = SolidColor(AppTheme.colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    val action = when (event.key) {
                        Key.Enter -> onSubmit
                        Key.DirectionUp -> onArrowUp
                        Key.DirectionDown -> onArrowDown
                        else -> null
                    }
                    val handled = action != null && event.type == KeyEventType.KeyDown
                    if (handled) action()
                    handled
                },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .heightIn(min = ControlSize.Medium.height)
                        .background(AppTheme.colors.surface, AppTheme.shapes.medium)
                        .border(1.dp, borderColor, AppTheme.shapes.medium)
                        .padding(horizontal = AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                ) {
                    if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, size = IconSize.Small)
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) Text(placeholder, emphasis = TextEmphasis.Disabled, maxLines = 1)
                        innerTextField()
                    }
                    trailing?.invoke()
                }
            },
        )
        if (error != null) Text(error, variant = TextVariant.Caption, tone = Tone.Error)
    }
}

@UiKitPreview
@Composable
private fun TextFieldPreview() = PreviewContext {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        TextField(value = "", onValueChange = {}, label = "Address", placeholder = "192.168.1.42:5555")
        TextField(value = "192.168.1.42:5555", onValueChange = {}, monospace = true)
        TextField(value = "abc", onValueChange = {}, label = "Port", error = "Not a number")
    }
}
