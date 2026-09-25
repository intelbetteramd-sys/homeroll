plugins {
    alias(libs.plugins.pixroost.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Spike S-05: the OAuth flow shared by Android and the desktop app.
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
