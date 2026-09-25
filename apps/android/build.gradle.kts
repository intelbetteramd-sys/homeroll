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
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":shared:designsystem"))
    implementation(libs.androidx.activity.compose)
}
