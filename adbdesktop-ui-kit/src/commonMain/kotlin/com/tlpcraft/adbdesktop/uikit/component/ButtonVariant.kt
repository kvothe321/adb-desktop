package com.tlpcraft.adbdesktop.uikit.component

enum class ButtonVariant {
    /** The main action of a view. Use at most one per area. */
    Primary,
    Secondary,

    /** Low emphasis, for toolbars and dense rows. */
    Ghost,

    /** Destructive actions such as uninstalling or clearing data. */
    Danger,
}
