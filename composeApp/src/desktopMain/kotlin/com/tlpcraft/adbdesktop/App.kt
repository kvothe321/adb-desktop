package com.tlpcraft.adbdesktop

import adb_desktop.composeapp.generated.resources.Res
import adb_desktop.composeapp.generated.resources.commands_icon
import adb_desktop.composeapp.generated.resources.phone_icon
import adb_desktop.composeapp.generated.resources.settings_icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tlpcraft.adbdesktop.di.appModules
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication

@Composable
fun App() {
    var isDark by remember { mutableStateOf(true) }
    KoinApplication(application = { modules(appModules) }) {
        AppTheme(darkTheme = isDark) {
            AppShell()

//            Row(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
//                SideMenu(isDark = isDark, onToggleTheme = { isDark = !isDark })
//
//                Divider(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .width(1.dp),
//                    color = colorScheme.outlineVariant
//                )
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .weight(0.70f)
//                ) {
//                    Text("Menu Area", style = TextStyle(color = colorScheme.onBackground))
//                    DevicesScreen()
//                }
//            }
        }
    }
}

@Composable
fun RowScope.SideMenu(isDark: Boolean, onToggleTheme: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .weight(0.30f)
    ) {
        val gradient = Brush.linearGradient(
            colors = listOf(colorScheme.primary, colorScheme.secondary)
        )

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "ADB Desktop",
            style = TextStyle(
                brush = gradient,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Android Debug Bridge GUI",
            style = TextStyle(color = colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(32.dp))
        MenuItem("Devices", imageResource = Res.drawable.phone_icon)
        MenuItem("Commands", imageResource = Res.drawable.commands_icon)
        MenuItem("Settings", imageResource = Res.drawable.settings_icon)

        Spacer(modifier = Modifier.weight(1f))
        MenuItem(
            text = if (isDark) "Switch to Light" else "Switch to Dark",
            imageResource = Res.drawable.settings_icon,
            onClick = onToggleTheme
        )
    }
}

@Composable
fun MenuItem(text: String, imageResource: DrawableResource, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = if (isHovered) colorScheme.surfaceVariant else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(PointerIcon.Hand),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(imageResource),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = TextStyle(fontSize = 14.sp, color = colorScheme.primary)
        )
    }
}

@UiKitPreview
@Composable
fun AppPreview() = PreviewContext {
    AppTheme {
        App()
    }
}
