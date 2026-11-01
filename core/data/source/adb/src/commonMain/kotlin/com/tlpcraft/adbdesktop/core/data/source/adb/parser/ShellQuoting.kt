package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** adb joins shell arguments with spaces and hands them to the device shell, so paths must be quoted for it. */
internal fun String.shellQuoted(): String = "'" + replace("'", "'\\''") + "'"
