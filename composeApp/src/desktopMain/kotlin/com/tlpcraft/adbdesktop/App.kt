package com.tlpcraft.adbdesktop

import adbdesktop.composeapp.generated.resources.Res
import adbdesktop.composeapp.generated.resources.commands_icon
import adbdesktop.composeapp.generated.resources.phone_icon
import adbdesktop.composeapp.generated.resources.settings_icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFF111827))) {
            SideMenu()

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color(0xFF1F2937)
            )

            // Right side - content (75% width)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.70f)
            ) {
                Text("Menu Area", style = TextStyle(color = Color.White))
            }
        }
    }
}

@Composable
fun RowScope.SideMenu() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .weight(0.30f)
    ) {
        val gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF2563EB), Color(0xFF9333EA))
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

        Text(modifier = Modifier.padding(horizontal = 16.dp), text = "Android Debug Bridge GUI", style = TextStyle(color = Color.Gray))

        Spacer(modifier = Modifier.height(32.dp))
        MenuItem("Devices", imageResource = Res.drawable.phone_icon)
        MenuItem("Commands", imageResource = Res.drawable.commands_icon)
        MenuItem("Settings", imageResource = Res.drawable.settings_icon)
    }
}

@Composable
fun MenuItem(text: String, imageResource: DrawableResource) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = if (isHovered) Color(0xFF1E293B) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(PointerIcon.Hand),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(imageResource),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFF60A4F9))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = TextStyle(fontSize = 14.sp, color = Color(0xFF60A4F9))
        )
    }
}
