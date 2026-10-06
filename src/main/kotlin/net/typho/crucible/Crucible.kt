package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.property.Property
import net.typho.crucible.script.ProjectConfigScript
import net.typho.crucible.task.Task
import net.typho.data_util.impl.PropertiesFormat
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
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
    fun <V : Any> finalizeOnRead(initial: () -> V) = object : ReadWriteProperty<Any, V> {
        private var value: V? = null
        private var read = false

        override fun getValue(thisRef: Any, property: KProperty<*>): V {
            read = true
            value?.let { return it }
            return initial().also { value = it }
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
            if (read) {
                throw IllegalStateException("Cannot set config value ${property.name} as it has already been read")
            }

            this.value = value
        }
    }

    @JvmStatic
    val projectRoot = Path(System.getProperty("user.dir"))

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

    @JvmField
    var stacktrace = debug || System.getProperty("crucible.stacktrace") == "true"
    @JvmStatic
    val kotlinVersion = Property { System.getProperty("crucible.kotlin_version") ?: "2.4.0" }.finalizeOnRead()

    @JvmStatic
    var projectGroup = Property { System.getProperty("crucible.project_group") ?: "" }.finalizeOnRead()
    @JvmStatic
    var projectName = Property { System.getProperty("crucible.project_name") ?: projectRoot.name }.finalizeOnRead()
    @JvmStatic
    var projectVersion = Property { System.getProperty("crucible.project_version") ?: "" }.finalizeOnRead()

    @JvmStatic
    val globalFolder = Property { System.getProperty("crucible.global_folder")?.let { Path(it) } ?: Path(System.getProperty("user.home")).resolve(".crucible") }.finalizeOnRead()
    @JvmStatic
    val globalCacheFolder = Property { globalFolder.value.resolve("caches") }.finalizeOnRead()
    @JvmStatic
    val mavenCacheFolder = Property { globalCacheFolder.value.resolve("maven") }.finalizeOnRead()

    @JvmStatic
    val projectCacheFolder = projectRoot.resolve(".crucible")
    @JvmStatic
    val ideInfoFile = projectCacheFolder.resolve("ide.json")
    @JvmStatic
    val sourceInputFolder = Property { projectRoot.resolve("src") }

    @JvmStatic
    val buildFolder = Property { System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: projectRoot.resolve("build") }.finalizeOnRead()
    @JvmStatic
    val jarOutputFolder = Property { buildFolder.value.resolve("jars") }.finalizeOnRead()
    @JvmStatic
    val sourceOutputFolder = Property { buildFolder.value.resolve("src") }.finalizeOnRead()
    @JvmStatic
    val configScriptFile = Property { System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: projectRoot.resolve("crucible.kts") }.finalizeOnRead()

    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies(repositories)

    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, error ->
            if (stacktrace) {
                error.printStackTrace()
            } else {
                LOG.error(buildString {
                    val queue = mutableListOf(error)

                    do {
                        val e = queue.removeFirst()
                        e.message?.let { append(it) }
                        queue.addAll(0, e.suppressed.asList())
                    } while (queue.isNotEmpty())
                })
            }
        }
    }

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

        loadConfigScript(configScriptFile)

        buildFolder.deleteRecursively()

        LOG.debug("Finished config phase in ${(System.currentTimeMillis() - startTime) / 1000f} seconds")

        for (task in args) {
            LOG.info("> Executing task '$task'")
            Task.get(task).invoke()
        }
    }
}