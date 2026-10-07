package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.PrintClasspathTask
import net.typho.crucible.deps.Repositories
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.ide.RefreshIDETask
import net.typho.crucible.property.Property
import net.typho.crucible.script.ProjectConfigScript
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JarTask
import net.typho.crucible.task.Task
import net.typho.data_util.impl.PropertiesFormat
import org.jetbrains.kotlin.com.intellij.psi.ResolveState.initial
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Supplier
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    init {
        System.getProperty("crucible.config_files")?.split(File.pathSeparatorChar)?.forEach { loadConfig(Path(it), true) }
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

    @JvmStatic
    val projectRoot = System.getProperty("crucible.project_root")?.let { Path(it) } ?: Path(System.getProperty("user.dir"))

    init {
        loadConfig(projectRoot.resolve("crucible.properties"), false)
    }

    @JvmField
    var debug = System.getProperty("crucible.debug") == "true" || System.getProperty("intellij.debug.agent") == "true"

    init {
        if (debug) {
            LOG.debug("Enabled crucible debug output")
        }
    }

    @JvmStatic
    val kotlinVersion = Property<String> { System.getProperty("crucible.kotlin_version") ?: "2.4.0" }.finalizeOnRead()

    @JvmField
    val projectGroup = Property<String> { System.getProperty("crucible.project_group") ?: "" }.finalizeOnRead()
    @JvmField
    val projectName = Property<String> { System.getProperty("crucible.project_name") ?: projectRoot.name }.finalizeOnRead()
    @JvmField
    val projectVersion = Property<String> { System.getProperty("crucible.project_version") ?: "" }.finalizeOnRead()

    @JvmStatic
    val javaVersion = Property<String> { System.getProperty("crucible.java_version") ?: "21" }.finalizeOnRead()

    @JvmStatic
    val globalFolder = Property<Path> { System.getProperty("crucible.global_folder")?.let { Path(it) } ?: Path(System.getProperty("user.home")).resolve(".crucible") }.finalizeOnRead()
    @JvmStatic
    val globalCacheFolder = Property<Path> { globalFolder().resolve("caches") }.finalizeOnRead()
    @JvmStatic
    val mavenCacheFolder = Property<Path> { globalCacheFolder().resolve("maven") }.finalizeOnRead()

    @JvmStatic
    val projectCacheFolder = projectRoot.resolve(".crucible")
    @JvmStatic
    val ideInfoFile = projectCacheFolder.resolve("ide.json")
    @JvmStatic
    val sourceInputFolder = Property<Path> { projectRoot.resolve("src") }

    @JvmStatic
    val buildFolder = Property<Path> { System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: projectRoot.resolve("build") }.finalizeOnRead()
    @JvmStatic
    val jarOutputFolder = Property<Path> { buildFolder().resolve("jars") }.finalizeOnRead()
    @JvmStatic
    val sourceOutputFolder = Property<Path> { buildFolder().resolve("src") }.finalizeOnRead()
    @JvmStatic
    val configScriptFile = Property<Path> { System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: projectRoot.resolve("crucible.kts") }.finalizeOnRead()

    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies(repositories)

    @JvmStatic
    fun loadConfigScript(path: Path) {
        if (path.exists()) {
            loadConfigScript(StringScriptSource(path.readText(), path.absolutePathString()))
        }
    }

    @JvmStatic
    fun loadConfigScript(script: SourceCode) {
        LOG.debug("Loading config script ${script.name}")
        val result = BasicJvmScriptingHost().eval(
            script,
            ProjectConfigScript,
            ScriptEvaluationConfiguration()
        )

        result.reports.forEach {
            when (it.severity) {
                ScriptDiagnostic.Severity.INFO, ScriptDiagnostic.Severity.DEBUG -> LOG.debug(it.message)
                ScriptDiagnostic.Severity.WARNING -> LOG.warn(it.message)
                ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> throw ConfigScriptException(it)
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        LOG.debug("Starting crucible at $projectRoot")

        loadConfigScript(configScriptFile())

        buildFolder().deleteRecursively()

        LOG.debug("Finished config phase in ${(System.currentTimeMillis() - startTime) / 1000f} seconds")

        for (task in args) {
            Task.get(task).invoke()
        }
    }
}