package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Immutable
import com.tlpcraft.adbdesktop.uikit.icon.IconName

/** One segment of [SegmentedTabs]. [hint] is added to the tooltip, such as a keyboard shortcut. */
@Immutable
data class TabItem(val label: String, val icon: IconName, val hint: String? = null)
