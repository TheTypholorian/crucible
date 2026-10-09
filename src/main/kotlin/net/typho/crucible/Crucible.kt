package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.property.Property
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.log.*
import net.typho.data_util.impl.PropertiesFormat
import java.io.File
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, exception ->
            if (exception is CompilationException || exception is ConfigScriptException) {
                error(exception.message)
            } else {
                exception.printStackTrace()
            }
        }

        System.getProperty("crucible.config_files")?.split(File.pathSeparatorChar)?.forEach { loadConfig(Path(it), true) }
    }

    fun loadConfig(path: Path, overwrite: Boolean = true) {
        if (path.exists()) {
            val properties = PropertiesFormat().read(path.readText())
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
    val projectRoot = CrucibleWrapper.projectRoot
    @JvmField
    val projectCacheFolder = CrucibleWrapper.projectCacheFolder

    init {
        loadConfig(projectRoot.resolve("crucible.properties"), false)
        debugEnabled = System.getProperty("crucible.debug") == "true" || System.getProperty("intellij.debug.agent") == "true"
    }

    @JvmField
    val projectGroup = Property<String> { System.getProperty("crucible.project_group") ?: "" }.finalizeOnRead()
    @JvmField
    val projectName = Property<String> { System.getProperty("crucible.project_name") ?: projectRoot.name }.finalizeOnRead()
    @JvmField
    val projectVersion = Property<String> { System.getProperty("crucible.project_version") ?: "" }.finalizeOnRead()

    @JvmField
    val javaVersion = Property<String> { System.getProperty("crucible.java_version") ?: "21" }.finalizeOnRead()

    @JvmField
    val ideInfoFile = projectCacheFolder.resolve("ide.json")
    @JvmField
    val scriptInfoFile = projectCacheFolder.resolve("scripts.bin")
    @JvmField
    val sourceInputFolder = Property<Path> { projectRoot.resolve("src") }

    @JvmField
    val buildFolder = Property<Path> { System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: projectRoot.resolve("build") }.finalizeOnRead()
    @JvmField
    val jarOutputFolder = Property<Path> { buildFolder().resolve("jars") }.finalizeOnRead()
    @JvmField
    val sourceOutputFolder = Property<Path> { buildFolder().resolve("src") }.finalizeOnRead()
    @JvmField
    val configScriptFile = Property<Path> { System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: projectRoot.resolve("crucible.kts") }.finalizeOnRead()

    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies(repositories)

    @JvmStatic
    fun loadConfigScript(path: Path) {
        if (path.exists()) {
            loadConfigScript(path.readText(), path.absolutePathString())
        }
    }

    @JvmStatic
    fun loadConfigScript(code: String, path: String) {
        debug("Loading config script $path")
        val result = BasicJvmScriptingHost().eval(
            StringScriptSource(code, path),
            AbstractProjectConfig.COMP_CONFIG,
            ScriptEvaluationConfiguration()
        )

        result.reports.forEach {
            when (it.severity) {
                ScriptDiagnostic.Severity.INFO, ScriptDiagnostic.Severity.DEBUG -> debug(it.message)
                ScriptDiagnostic.Severity.WARNING -> warn(it.message)
                ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> throw ConfigScriptException(it)
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        debug("Starting crucible at $projectRoot")

        loadConfigScript(configScriptFile())

        debug("Finished config phase in ${(System.currentTimeMillis() - startTime) / 1000f} seconds")

        for (task in args) {
            Task.get(task).invoke()
        }
    }
}