// Builds PixroostKit, the framework the Xcode project in apps/ios links against.
// Xcode calls :apps:ios-framework:embedAndSignAppleFrameworkForXcode before compiling Swift.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "PixroostKit"
            isStatic = true
        }
    }

    sourceSets.iosMain.dependencies {
        implementation(project(":shared:core"))
        implementation(project(":shared:designsystem"))
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
    }
}
