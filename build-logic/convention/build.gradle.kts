plugins {
    `kotlin-dsl`
}

group = "app.pixroost.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = libs.plugins.pixroost.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("compose") {
            id = libs.plugins.pixroost.compose.get().pluginId
            implementationClass = "ComposeConventionPlugin"
        }
        register("androidApplication") {
            id = libs.plugins.pixroost.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("desktopApplication") {
            id = libs.plugins.pixroost.desktop.application.get().pluginId
            implementationClass = "DesktopApplicationConventionPlugin"
        }
    }
}
