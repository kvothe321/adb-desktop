package com.tlpcraft.adbdesktop.plugin.convention.linting.ktlint

import com.tlpcraft.adbdesktop.plugin.convention.hooks.precommit.ensurePreCommitGitHookInstalled
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

class GlobalKtlint : Plugin<Project> {
    override fun apply(target: Project) {
        println("[BUILD-LOGIC] - Applying Global Ktlint Convention Plugin")
        with(target) {
            allprojects {
                pluginManager.apply(KtlintPlugin::class.java)
                extensions.configure(KtlintExtension::class.java, ktlintConfiguration)
            }

            ensurePreCommitGitHookInstalled()
        }
    }

    private val ktlintConfiguration: KtlintExtension.() -> Unit = {
        filter {
            exclude { element ->
                element.file.path.contains("generated")
            }
            include("**/kotlin/**")
        }
    }
}
