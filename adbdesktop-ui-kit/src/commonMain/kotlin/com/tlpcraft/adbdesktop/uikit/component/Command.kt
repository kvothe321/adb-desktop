package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Immutable
import com.tlpcraft.adbdesktop.uikit.icon.IconName

/** Something the [CommandPalette] can do. Commands are listed under their [group], in the order given. */
@Immutable
data class Command(val id: String, val title: String, val icon: IconName, val group: String, val hint: String? = null)
