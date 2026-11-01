package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch

/** Click, hover and hand cursor in one place, so every interactive component behaves the same on desktop. */
internal fun Modifier.interactive(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    role: Role,
    onClick: () -> Unit,
): Modifier = this
    .hoverable(interactionSource, enabled)
    .clickable(interactionSource = interactionSource, indication = PressIndication, enabled = enabled, role = role, onClick = onClick)
    .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)

/** A quiet dimming while pressed, instead of a ripple: feedback that stays out of the way. */
private data object PressIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = PressNode(interactionSource)
}

private class PressNode(private val interactionSource: InteractionSource) : Modifier.Node(), DrawModifierNode {
    private var isPressed = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                isPressed = when (interaction) {
                    is PressInteraction.Press -> true
                    is PressInteraction.Release, is PressInteraction.Cancel -> false
                    else -> isPressed
                }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isPressed) drawRect(PRESSED_OVERLAY)
    }
}

private const val PRESSED_ALPHA = 0.12f
private val PRESSED_OVERLAY = Color.Gray.copy(alpha = PRESSED_ALPHA)
