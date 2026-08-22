package com.tlpcraft.adbdesktop.core.data.source.preferences

import java.io.File

private const val LINUX_DIRECTORY_NAME = "adb-desktop"
private const val DIRECTORY_NAME = "ADB Desktop"

/** Per-user directory the operating system expects applications to keep their settings in. */
internal fun settingsDirectory(
    osName: String = System.getProperty("os.name"),
    userHome: String = System.getProperty("user.home"),
    environment: (String) -> String? = System::getenv,
): File {
    val os = osName.lowercase()
    return when {
        "win" in os && "darwin" !in os -> File(environment("APPDATA") ?: "$userHome/AppData/Roaming", DIRECTORY_NAME)
        "mac" in os || "darwin" in os -> File(userHome, "Library/Application Support/$DIRECTORY_NAME")
        else -> File(environment("XDG_CONFIG_HOME")?.takeIf(String::isNotBlank) ?: "$userHome/.config", LINUX_DIRECTORY_NAME)
    }
}
