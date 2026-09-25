// Plugins are declared here once, so every module and build-logic share one version of each.
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// Style and static analysis run once over the whole repository, so a new module is covered
// without extra setup. `check` depends on both.
// Spotless reads .editorconfig but not ktlint_code_style from it, so the style is repeated here.
val ktlintStyle = mapOf("ktlint_code_style" to "intellij_idea")

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintStyle)
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintStyle)
    }
}

detekt {
    source.setFrom(
        fileTree(rootDir) {
            include("apps/*/src/**/*.kt", "shared/**/src/**/*.kt", "build-logic/*/src/**/*.kt")
            exclude("**/build/**")
        },
    )
    config.setFrom("config/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}

// One coverage report for all shared modules: ./gradlew koverHtmlReport
dependencies {
    subprojects.filter { it.path.startsWith(":shared:") }.forEach { kover(project(it.path)) }
}
