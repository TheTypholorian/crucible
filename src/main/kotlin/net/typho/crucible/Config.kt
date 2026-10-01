package net.typho.crucible

import net.typho.data_util.impl.PropertiesFormat
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText

object Config {
    init {
        System.getProperty("crucible.config_files")?.split(File.pathSeparatorChar)?.forEach { loadConfig(Paths.get(it), true) }
    }

    fun loadConfig(path: Path, overwrite: Boolean = true) {
        if (path.exists()) {
            val properties = PropertiesFormat().read(path.readText())
            val target = System.getProperties()

            properties.forEach { (key, value) ->
                if (overwrite) {
                    target.put(key, value)?.let { old ->
                        LOG.warn("Property $key was already set to $old but ${path.name} set it to $value")
                    }
                } else {
                    target.putIfAbsent(key, value)
                }
            }
        }
    }

    @JvmField
    val PROJECT_ROOT = (System.getProperty("crucible.project_root")?.let { Path.of(it) } ?: Path.of("")).absolute()

    init {
        loadConfig(PROJECT_ROOT.resolve("crucible.properties"), false)
    }

    @JvmField
    val DEBUG = System.getProperty("crucible.debug") == "true" || System.getProperty("intellij.debug.agent") == "true"

    init {
        if (DEBUG) {
            LOG.debug("Enabled crucible debug output")
        }
    }

    @JvmField
    val STACKTRACE = DEBUG || System.getProperty("crucible.stacktrace") == "true"
    @JvmField
    val KOTLIN_VERSION = System.getProperty("crucible.kotlin_version") ?: "2.4.0"

    @JvmField
    val GLOBAL_FOLDER = System.getProperty("crucible.global_folder")?.let { Path.of(it) } ?: Path.of(System.getProperty("user.home")).resolve(".crucible")
    @JvmField
    val CACHE_FOLDER = GLOBAL_FOLDER.resolve("caches")
    @JvmField
    val MAVEN_CACHE_FOLDER = CACHE_FOLDER.resolve("maven")

    @JvmField
    val BUILD_FOLDER = System.getProperty("crucible.build_folder")?.let { Path.of(it).absolute() } ?: PROJECT_ROOT.resolve("build")
    @JvmField
    val SOURCE_OUTPUT_FOLDER = BUILD_FOLDER.resolve("src")
    @JvmField
    val CONFIG_SCRIPT_FILE = System.getProperty("crucible.config_script")?.let { Path.of(it).absolute() } ?: PROJECT_ROOT.resolve("crucible.kts")
}