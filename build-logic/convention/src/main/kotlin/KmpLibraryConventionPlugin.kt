import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Kotlin Multiplatform library for every Pixroost target:
 * Android, desktop (JVM) and iOS. iOS compiles only on macOS.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(JDK_VERSION)

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = pixroostNamespace
                compileSdk = libs.intVersion("androidCompileSdk")
                minSdk = libs.intVersion("androidMinSdk")
                compilerOptions {
                    // Android runs Java 17 bytecode regardless of the JDK used for the build.
                    jvmTarget.set(JvmTarget.JVM_17)
                }
                // Runs commonTest on the JVM against the Android variant too.
                withHostTest {}
            }

            jvm("desktop")
            iosArm64()
            iosSimulatorArm64()

            sourceSets.getByName("commonTest").dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
