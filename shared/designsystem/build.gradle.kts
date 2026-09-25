plugins {
    alias(libs.plugins.pixroost.kmp.library)
    alias(libs.plugins.pixroost.compose)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":shared:core"))
    }
}
