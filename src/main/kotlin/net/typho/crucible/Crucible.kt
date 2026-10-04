package net.typho.crucible

import net.typho.crucible.deps.Classpath
import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.script.ProjectConfigScript
import net.typho.crucible.task.Task
import net.typho.data_util.impl.PropertiesFormat
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.ExperimentalPathApi
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
    val projectRoot by finalizeOnRead { (System.getProperty("crucible.project_root")?.let { Path.of(it) } ?: Path.of("")).absolute() }

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
    val kotlinVersion by finalizeOnRead { System.getProperty("crucible.kotlin_version") ?: "2.4.0" }

    @JvmStatic
    var projectGroup by finalizeOnRead { System.getProperty("crucible.project_group") ?: "" }
    @JvmStatic
    var projectName by finalizeOnRead { System.getProperty("crucible.project_name") ?: projectRoot.name }
    @JvmStatic
    var projectVersion by finalizeOnRead { System.getProperty("crucible.project_version") ?: "" }

    @JvmStatic
    val globalFolder by finalizeOnRead { System.getProperty("crucible.global_folder")?.let { Path.of(it) } ?: Path.of(System.getProperty("user.home")).resolve(".crucible") }
    @JvmStatic
    val cacheFolder by finalizeOnRead { globalFolder.resolve("caches") }
    @JvmStatic
    val mavenCacheFolder by finalizeOnRead { cacheFolder.resolve("maven") }

    @JvmStatic
    val projectCacheFolder by finalizeOnRead { projectRoot.resolve(".crucible") }
    @JvmStatic
    val sourceInputFolder by finalizeOnRead { projectRoot.resolve("src") }

    @JvmStatic
    val buildFolder by finalizeOnRead { System.getProperty("crucible.build_folder")?.let { Path.of(it).absolute() } ?: projectRoot.resolve("build") }
    @JvmStatic
    val jarOutputFolder by finalizeOnRead { buildFolder.resolve("jars") }
    @JvmStatic
    val sourceOutputFolder by finalizeOnRead { buildFolder.resolve("src") }
    @JvmStatic
    val configScriptFile by finalizeOnRead { System.getProperty("crucible.config_script")?.let { Path.of(it).absolute() } ?: projectRoot.resolve("crucible.kts") }

    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies()
    @JvmStatic
    var classpath = Classpath()

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

        classpath += dependencies.map {
            repositories.find(it)?.path
                ?: throw DependencyNotFoundException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        }
        LOG.debug("Class path: ${classpath.entries}")
        LOG.debug("Finished config phase in ${(System.currentTimeMillis() - startTime) / 1000f} seconds")

        for (task in args) {
            LOG.info("> Executing task '$task'")
            Task.get(task).invoke()
        }
    }
}