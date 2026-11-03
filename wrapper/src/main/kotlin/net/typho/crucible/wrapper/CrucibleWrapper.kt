package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.warn
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.MavenUtil
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path
import java.util.Properties
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.outputStream

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

    const val VERSION = "1.2.3"

    @JvmField
    val projectRoot = System.getProperty("crucible.project_root")?.let { Path(it) } ?: Path(System.getProperty("user.dir"))
    @JvmField
    val projectCacheFolder = projectRoot.resolve(".crucible")
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

    @JvmStatic
    fun start(args: Array<String>) {
        val loader = CrucibleClassLoader
        Thread.currentThread().contextClassLoader = loader

        // the inner jar should be very small so this is fine
        val innerJar = ClassLoader.getSystemResource("crucible/crucible.jar") ?: throw IllegalStateException("crucible.jar not found inside wrapper jar, this should never happen")
        val extractedJar = projectCacheFolder.resolve("crucible.jar")
        innerJar.openStream().use { input ->
            extractedJar.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        loader.addURL(extractedJar.toUri().toURL())

        val repositories = listOf(MavenUtil.MAVEN_CENTRAL, MavenUtil.TYPHO_NET)
        loader.addWithDependencies(
            repositories,
            listOf(
                ArtifactCoords("net.typho", "crucible.ide_data", "2.1.1"),
                ArtifactCoords("net.typho", "misc_util", "1.1.0"),

                ArtifactCoords("org.vineflower", "vineflower", "1.12.0"),
                ArtifactCoords("org.semver4j", "semver4j", "6.0.0"),
                ArtifactCoords("org.jetbrains.kotlin", "kotlin-stdlib", "2.4.21"),
                ArtifactCoords("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "2.4.21"),
                ArtifactCoords("org.jetbrains.kotlin", "kotlin-scripting-jvm", "2.4.21"),
                ArtifactCoords("org.jetbrains.kotlin", "kotlin-scripting-jvm-host", "2.4.21")
            )
        )

        loader.addSources(repositories, ArtifactCoords("net.typho", "crucible", VERSION))
        loader.addSources(repositories, ArtifactCoords("net.typho", "crucible.wrapper", VERSION))
        loader.addSources(repositories, ArtifactCoords("net.typho", "maven_util", "1.5.1"))

        try {
            loader.loadClass("net.typho.crucible.Crucible")
                .getMethod("start", Array<String>::class.java)
                .invoke(null, args)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}