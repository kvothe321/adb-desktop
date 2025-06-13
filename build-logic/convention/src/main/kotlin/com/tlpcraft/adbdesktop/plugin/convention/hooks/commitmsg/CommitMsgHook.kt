package com.tlpcraft.adbdesktop.plugin.convention.hooks.commitmsg

import com.tlpcraft.adbdesktop.plugin.convention.linting.commitlint.getCommitMsgGitHookScriptContent
import org.gradle.api.Project

fun Project.ensureCommitMsgGitHookInstalled() {
    val hookFile = file("${rootProject.projectDir}/.git/hooks/commit-msg")
    val expectedContent = getCommitMsgGitHookScriptContent()

    val needsUpdate = hookFile.exists().not() || hookFile.readText() != expectedContent

    if (needsUpdate) {
        installCommitMsgGitHook(expectedContent)
    } else {
        println("[BUILD LOGIC] - Git commit-msg hook is up-to-date")
    }
}

private fun Project.installCommitMsgGitHook(scriptContent: String) {
    val scriptProvider = resources.text.fromString(scriptContent)

    copy {
        from(scriptProvider)
        into("${rootProject.rootDir}/.git/hooks")
        rename { "commit-msg" }
        filePermissions {
            unix("rwxrwxr-x")
        }
    }

    println("[BUILD LOGIC] - Installed commit-msg Git hook for commit message linting")
}
