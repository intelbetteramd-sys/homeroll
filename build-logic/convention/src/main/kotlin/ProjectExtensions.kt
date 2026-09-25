import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** JDK used to compile and run everything except the Android bytecode. See docs/process/jdk.md. */
internal const val JDK_VERSION = 25

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

/** `:shared:sources:yandex` → `app.pixroost.sources.yandex`, `:apps:android` → `app.pixroost.android`. */
internal val Project.pixroostNamespace: String
    get() = "app.pixroost." + path.split(':').filter { it.isNotEmpty() }.drop(1)
        .joinToString(".") { it.replace("-", "") }
