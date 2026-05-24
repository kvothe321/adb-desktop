package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role

/** Click, hover and hand cursor in one place, so every interactive component behaves the same on desktop. */
internal fun Modifier.interactive(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    role: Role,
    onClick: () -> Unit,
): Modifier = this
    .hoverable(interactionSource, enabled)
    .clickable(interactionSource = interactionSource, indication = ripple(), enabled = enabled, role = role, onClick = onClick)
    .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
