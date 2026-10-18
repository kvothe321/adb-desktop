plugins {
    alias(libs.plugins.android.application)
}

// Code that runs on the device through app_process, for what adb cannot ask Android directly. Its APK is never
// installed: core:data:source:adb bundles it and pushes it when needed.
android {
    namespace = "com.tlpcraft.adbdesktop.helper"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tlpcraft.adbdesktop.helper"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

// The helper is plain Java, so the Kotlin library the Android plugin adds would only make the APK bigger.
configurations.matching { it.name.endsWith("RuntimeClasspath") }.configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

val helperApk by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(helperApk.name, layout.buildDirectory.file("outputs/apk/release/device-helper-release-unsigned.apk")) {
        builtBy("assembleRelease")
    }
}
