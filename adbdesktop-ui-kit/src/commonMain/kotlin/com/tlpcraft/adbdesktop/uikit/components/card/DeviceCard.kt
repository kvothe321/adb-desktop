package com.tlpcraft.adbdesktop.uikit.components.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.uikit.components.text.BodySmallText
import com.tlpcraft.adbdesktop.uikit.components.text.LabelSmallText
import com.tlpcraft.adbdesktop.uikit.components.text.TitleMediumText
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

private val OnlineGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)

@Composable
fun DeviceCard(
    deviceName: String,
    androidVersion: String?,
    batteryLevel: Int?,
    cpuInfo: CpuInfo?,
    isOnline: Boolean,
    statusText: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(dimensions.radius.lg)

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            colorScheme.primaryContainer.copy(alpha = 0.22f)
        } else {
            colorScheme.surface
        },
        label = "containerColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else Color.Transparent,
        label = "borderColor",
    )

    ElevatedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) dimensions.stroke.md else dimensions.stroke.thin,
                color = borderColor,
                shape = shape,
            ),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp,
        ),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(dimensions.spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TitleMediumText(text = deviceName, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(dimensions.spacing.xs))
                    BodySmallText(
                        text = androidVersion ?: "Android —",
                        color = colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
                Spacer(Modifier.width(dimensions.spacing.sm))
                StatusIndicator(isOnline = isOnline, label = statusText)
            }

            if (isOnline) {
                Spacer(Modifier.height(dimensions.spacing.md))
                HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(dimensions.spacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.sm)) {
                    BatteryRow(level = batteryLevel)
                    CpuRow(info = cpuInfo)
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(isOnline: Boolean, label: String) {
    val color = if (isOnline) OnlineGreen else colorScheme.error
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        LabelSmallText(text = label, color = color)
    }
}

@Composable
private fun BatteryRow(level: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
    ) {
        LabelSmallText(
            text = "Battery",
            color = colorScheme.onSurface.copy(alpha = 0.55f),
        )
        if (level != null) {
            val barColor = when {
                level >= 60 -> OnlineGreen
                level >= 20 -> WarningOrange
                else -> colorScheme.error
            }
            LinearProgressIndicator(
                progress = { level / 100f },
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(dimensions.radius.full)),
                color = barColor,
                trackColor = colorScheme.onSurface.copy(alpha = 0.12f),
            )
            LabelSmallText(text = "$level%", color = barColor)
        } else {
            LabelSmallText(text = "—", color = colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun CpuRow(info: CpuInfo?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
    ) {
        LabelSmallText(
            text = "CPU",
            color = colorScheme.onSurface.copy(alpha = 0.55f),
        )
        if (info != null) {
            val usage = info.usagePercent.coerceIn(0f, 100f)
            val barColor = when {
                usage < 60f -> OnlineGreen
                usage < 80f -> WarningOrange
                else -> colorScheme.error
            }
            LinearProgressIndicator(
                progress = { usage / 100f },
                modifier = Modifier
                    .width(80.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(dimensions.radius.full)),
                color = barColor,
                trackColor = colorScheme.onSurface.copy(alpha = 0.12f),
            )
            LabelSmallText(text = "${info.usagePercent.toInt()}%", color = barColor)
        } else {
            LabelSmallText(text = "—", color = colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@UiKitPreview
@Composable
private fun DeviceCardOnlineSelectedPreview() = PreviewContext {
    DeviceCard(
        deviceName = "emulator-5554",
        androidVersion = "Android 14",
        batteryLevel = 87,
        cpuInfo = CpuInfo(usagePercent = 23.4f, coreCount = 8, maxFrequencyMHz = 2400),
        isOnline = true,
        statusText = "Online",
        isSelected = true,
    )
}

@UiKitPreview
@Composable
private fun DeviceCardOnlineLowBatteryPreview() = PreviewContext {
    DeviceCard(
        deviceName = "R5CT10ABCDE",
        androidVersion = "Android 13",
        batteryLevel = 14,
        cpuInfo = CpuInfo(usagePercent = 75f, coreCount = 6, maxFrequencyMHz = 1800),
        isOnline = true,
        statusText = "Online",
        isSelected = false,
    )
}

@UiKitPreview
@Composable
private fun DeviceCardOfflinePreview() = PreviewContext {
    DeviceCard(
        deviceName = "192.168.1.42:5555",
        androidVersion = null,
        batteryLevel = null,
        cpuInfo = null,
        isOnline = false,
        statusText = "Offline",
        enabled = false,
    )
}

@UiKitPreview
@Composable
private fun DeviceCardLoadingStatsPreview() = PreviewContext {
    DeviceCard(
        deviceName = "emulator-5556",
        androidVersion = "Android 12",
        batteryLevel = null,
        cpuInfo = null,
        isOnline = true,
        statusText = "Online",
    )
}
