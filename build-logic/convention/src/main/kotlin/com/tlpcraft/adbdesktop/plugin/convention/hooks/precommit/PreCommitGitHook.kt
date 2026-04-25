package com.tlpcraft.adbdesktop.plugin.convention.hooks.precommit

import com.tlpcraft.adbdesktop.plugin.convention.linting.ktlint.getKtlintPreCommitGitHookScriptContent
import org.gradle.api.Project

fun Project.ensurePreCommitGitHookInstalled() {
    val hookFile = file("${rootProject.projectDir}/.git/hooks/pre-commit")
    val expectedContent = getKtlintPreCommitGitHookScriptContent()

    val needsUpdate = hookFile.exists().not() || hookFile.readText() != expectedContent

    if (needsUpdate) {
        installPreCommitGitHook(expectedContent)
    } else {
        println("[BUILD LOGIC] - Git pre-commit hook is up-to-date")
    }
}

private fun Project.installPreCommitGitHook(scriptContent: String) {
    val scriptProvider = resources.text.fromString(scriptContent)

    copy {
        from(scriptProvider)
        into("${rootProject.rootDir}/.git/hooks")
        rename { "pre-commit" }
        filePermissions {
            unix("rwxrwxr-x")
        }
    }

    println("[BUILD LOGIC] - Installed Git pre-commit hook for ktlintFormat")
}
