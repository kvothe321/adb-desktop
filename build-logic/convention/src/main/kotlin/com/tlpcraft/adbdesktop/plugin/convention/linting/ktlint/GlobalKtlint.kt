package com.tlpcraft.adbdesktop.plugin.convention.linting.ktlint

import com.tlpcraft.adbdesktop.plugin.convention.hooks.precommit.ensurePreCommitGitHookInstalled
import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

/** Applies ktlint with the Compose rule set to every project. Rule settings live in `.editorconfig`. */
class GlobalKtlint : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val engineVersion = libs.findVersion("ktlint").get().requiredVersion
            val composeRules = libs.library("compose-rules-ktlint")

            allprojects {
                pluginManager.apply(KtlintPlugin::class.java)
                extensions.configure(KtlintExtension::class.java) {
                    version.set(engineVersion)
                    filter {
                        exclude { element -> element.file.path.contains("generated") }
                        include("**/kotlin/**")
                    }
                }
                dependencies.add("ktlintRuleset", composeRules)
            }

            ensurePreCommitGitHookInstalled()
        }
    }
}
