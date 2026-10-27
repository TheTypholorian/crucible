package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.warn
import org.eclipse.aether.repository.RepositoryPolicy
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path
import java.util.Properties
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name

object CrucibleWrapper {
    fun loadConfig(path: Path, overwrite: Boolean = true) {
        if (path.exists()) {
            val properties = Properties()
            path.inputStream().use { properties.load(it) }
            val target = System.getProperties()

            properties.forEach { (key, value) ->
                if (overwrite) {
                    target.put(key, value)?.let { old ->
                        warn("Property $key was already set to $old but ${path.name} set it to $value")
                    }
                } else {
                    target.putIfAbsent(key, value)
                }
            }
        }
    }

    @JvmField
    val mavenLocal = Path("${System.getProperty("user.home")}/.m2/repository").toUri().toString()
    @JvmField
    val projectRoot = System.getProperty("crucible.project_root")?.let { Path(it) } ?: Path(System.getProperty("user.dir"))
    @JvmField
    val projectPropertiesFile = projectRoot.resolve("crucible.properties")

    init {
        loadConfig(projectPropertiesFile, false)
    }

    @JvmField
    val globalCacheFolder = System.getProperty("crucible.global_cache_folder")?.let { Path(it) } ?: Path(System.getProperty("user.home"), ".crucible")

    init {
        loadConfig(globalCacheFolder.resolve("crucible.properties"), false)
    }

    @JvmField
    val mavenCacheFolder = System.getProperty("crucible.maven_cache_folder")?.let { Path(it) } ?: globalCacheFolder.resolve("caches").resolve("maven")
    @JvmField
    var debug = System.getProperty("crucible.debug") == "true" || System.getProperty("intellij.debug.agent") == "true"
    @JvmField
    val crucibleVersion = System.getProperty("crucible.version") ?: throw IllegalStateException("Must set property 'crucible.version' (via system properties or by setting it in $projectPropertiesFile")
    @JvmField
    val hardCaching = System.getProperty("crucible.hard_caching")?.toBoolean() ?: false

    @JvmStatic
    fun main(args: Array<String>) {
        val loader = CrucibleClassLoader
        Thread.currentThread().contextClassLoader = loader

        CrucibleClassLoader.load(
            listOf(
                mavenLocal,
                "https://repo1.maven.org/maven2",
                "https://typho.net/maven"
            ),
            listOf(
                "net.typho:crucible:$crucibleVersion"
            )
        )

        try {
            loader.loadClass("net.typho.crucible.Crucible")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, args)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}