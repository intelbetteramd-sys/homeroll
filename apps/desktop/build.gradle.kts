import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.pixroost.desktop.application)
}

dependencies {
    implementation(project(":shared:core"))
    implementation(project(":shared:designsystem"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "app.pixroost.desktop.MainKt"
        nativeDistributions {
            // Installers are worked out in spike S-07 (step 1.10).
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Pixroost"
        }
    }
}
