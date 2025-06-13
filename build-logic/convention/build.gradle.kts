plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("globalKtlint") {
            id = "com.tlpcraft.adbdesktop.global.ktlint"
            implementationClass = "GlobalKtlint"
        }
    }
}
