package com.tlpcraft.adbdesktop.domain.model

/**
 * Determines which subset of installed packages `pm list packages` returns.
 *
 * Each variant maps directly to a `pm list packages` flag:
 * - [ALL]      → no flag (every installed package)
 * - [USER]     → `-3`   (third-party / user-installed packages)
 * - [SYSTEM]   → `-s`   (packages shipped with the ROM)
 * - [DISABLED] → `-d`   (packages that have been disabled)
 */
enum class AppFilter(val label: String) {
    ALL("All"),
    USER("User"),
    SYSTEM("System"),
    DISABLED("Disabled")
}
