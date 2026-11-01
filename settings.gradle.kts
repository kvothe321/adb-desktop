@file:Suppress("UnstableApiUsage")

rootProject.name = "adb-desktop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    frameworkLayer,
    *core,
    *features,
)

include(":adbdesktop-ui-kit")
include(":device-helper")

val frameworkLayer
    get() = "desktopApp"

val features
    get() = listOf(
        "feature:devices",
        "feature:apps",
        "feature:files",
        "feature:logcat",
        "feature:shell",
        "feature:screen",
        "feature:settings",
    ).toTypedArray()

val core
    get() = listOf(
        "core:common",
        "core:logger",
        "core:domain",
        "core:testing",
        "core:data:repository",
        "core:data:source:adb",
        "core:data:source:preferences",
    ).toTypedArray()
