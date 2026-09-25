plugins {
    alias(libs.plugins.pixroost.android.application)
}

base {
    // APK files are named pixroost-debug.apk and so on, which is easier to find in Telegram.
    archivesName = "pixroost"
}

android {
    defaultConfig {
        applicationId = "app.pixroost"
        // Spike S-03: installs next to the regular app instead of over it.
        applicationIdSuffix = ".spike.transfer"
        versionCode = 1
        versionName = "0.1.0-spike.s03"
    }
}

dependencies {
    implementation(project(":shared:designsystem"))
    implementation(project(":shared:core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.ktor.client.okhttp)
}
