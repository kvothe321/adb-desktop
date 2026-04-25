package com.tlpcraft.adbdesktop.uikit.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.tlpcraft.adbdesktop.uikit.components.text.BodyMediumText
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

@Composable
fun NavigationDrawerItem(
    label: String,
    iconVector: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val contentColor = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface

    NavigationDrawerItem(
        label = { BodyMediumText(label, color = contentColor) },
        icon = { Icon(iconVector, null, tint = contentColor) },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(dimensions.radius.md),
    )
}

@UiKitPreview
@Composable
private fun NavigationDrawerItemPreview() = PreviewContext {
    Column {
        NavigationDrawerItem(
            label = "Home",
            iconVector = Icons.Default.Home,
            selected = true,
        )
        NavigationDrawerItem(
            label = "Home",
            iconVector = Icons.Default.Home,
            selected = false,
        )
    }
}
