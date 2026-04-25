package com.tlpcraft.adbdesktop.plugin.convention.linting.ktlint

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

fun getKtlintPreCommitGitHookScriptContent(): String {
    val isWindows = DefaultNativePlatform.getCurrentOperatingSystem().isWindows
    return getScriptContent(isWindows)
}

private fun getScriptContent(isWindows: Boolean) = """
    #!${if (isWindows) "/bin/sh" else "/bin/bash"}
    # Pre-commit hook to enforce code style using ktlint

    echo "Running ktlint format..."

    ./gradlew${if (isWindows) ".bat" else ""} ktlintFormat --daemon

    ktlintFormatStatus=${'$'}?

    if [ ${'$'}ktlintFormatStatus -ne 0 ]; then
        echo ""
        echo "*********************************************************"
        echo "Code style violations detected. Commit aborted."
        echo "To identify the issues: ./gradlew ktlintCheck"
        echo "To try automatically solving the issues: ./gradlew ktlintFormat"
        echo "*********************************************************"
        exit 1
    fi

    # Stage any files that ktlintFormat may have changed
    git diff --name-only | while read file; do
        if [ -f "${'$'}file" ]; then
            git add "${'$'}file"
            echo "Re-staged: ${'$'}file"
        fi
    done

    exit 0
    """.trimIndent()
