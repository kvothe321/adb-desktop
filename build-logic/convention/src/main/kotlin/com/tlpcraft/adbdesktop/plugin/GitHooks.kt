package com.tlpcraft.adbdesktop.plugin

import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.Project
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

internal fun Project.ensureGitHookInstalled() {
    // Check if the pre-commit hook file exists and matches the one from the repository
    val hookFile = file("${rootProject.projectDir}/.git/hooks/pre-commit")
    val suffix = if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) "windows" else "unix"
    val scriptPath = "${rootProject.rootDir}/scripts/pre-commit-$suffix"
    val hookFilePath = Paths.get(hookFile.absolutePath)
    val scriptFilePath = Paths.get(scriptPath)

    // If the hook file does not exist or does not match, install or update it
    if (!hookFile.exists() || !Files.readAllBytes(hookFilePath).contentEquals(Files.readAllBytes(scriptFilePath))) {
        installGitHook()
    } else {
        println("[BUILD LOGIC] - Git pre-commit hook is up-to-date")
    }
}

private fun Project.installGitHook() {
    // Determine the appropriate script based on the operating system
    val suffix = if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) "windows" else "unix"
    val scriptPath = "${rootProject.rootDir}/scripts/pre-commit-$suffix"

    // Copy the pre-commit script to the .git/hook directory
    copy {
        from(scriptPath)
        into("${rootProject.rootDir}/.git/hooks")
        rename("pre-commit-$suffix", "pre-commit")
        filePermissions {
            unix("rwxrwxr-x")
        }
    }
    println("[BUILD LOGIC] - Installed Git pre-commit hook")
}
