package net.typho.crucible

import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.script.PluginConfigScript
import net.typho.crucible.script.ProjectConfigScript
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.log.*
import net.typho.data_util.SerializedData
import net.typho.data_util.json.JsonFormat
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible : PropertyContainer() {
    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, exception ->
            if (exception is CompilationException || exception is ConfigScriptException) {
                error(exception.message)
            } else {
                exception.printStackTrace()
            }
        }
    }

    val group by lazyProperty {
        System.getProperty("crucible.project_group") ?: ""
    }.finalizeOnRead()
    val name by lazyProperty {
        System.getProperty("crucible.project_name") ?: CrucibleWrapper.projectRoot.name
    }.finalizeOnRead()
    val version by lazyProperty {
        System.getProperty("crucible.project_version") ?: ""
    }.finalizeOnRead()

    val javaVersion by lazyProperty {
        System.getProperty("crucible.java_version") ?: "21"
    }.finalizeOnRead()

    @JvmField
    val ideInfoFile = CrucibleWrapper.projectCacheFolder.resolve("ide.json")
    @JvmField
    val scriptInfoFile = CrucibleWrapper.projectCacheFolder.resolve("scripts.bin")
    val sourceInputFolder by lazyProperty {
        CrucibleWrapper.projectRoot.resolve("src")
    }.finalizeOnRead()

    val buildFolder by lazyProperty {
        System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: CrucibleWrapper.projectRoot.resolve("build")
    }.finalizeOnRead()
    val pluginsScriptFile by lazyProperty {
        System.getProperty("crucible.plugin_script")?.let { Path(it).absolute() } ?: CrucibleWrapper.projectRoot.resolve("plugins.crucible.kts")
    }.finalizeOnRead()
    val configScriptFile by lazyProperty {
        System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: CrucibleWrapper.projectRoot.resolve("crucible.kts")
    }.finalizeOnRead()
    val dependencyVersionsFile by lazyProperty {
        System.getProperty("crucible.dependency_versions_file")?.let { Path(it).absolute() } ?: CrucibleWrapper.projectRoot.resolve("deps.json")
    }.finalizeOnRead()

    @JvmField
    val modules = NamedContainer<Module, SimpleModule> { SimpleModule(it) }

    @JvmField
    val fallbackDependencyVersions = mutableMapOf<String, String>()

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadDependencyVersions(path: Path): Map<String, String> {
        return if (path.exists()) {
            JsonFormat.read(SerializedData(path)) as Map<String, String>
        } else {
            mapOf()
        }
    }

    internal val scriptFiles = mutableMapOf<Path, ScriptCompilationConfiguration>()

    @JvmStatic
    fun getDependencyVersion(groupId: String, artifactId: String): String {
        return fallbackDependencyVersions["$groupId:$artifactId"] ?: throw NullPointerException("No version specified for $groupId:$artifactId, and no fallback version specified in ${dependencyVersionsFile()}")
    }

    @JvmStatic
    fun loadScript(path: Path) = eval(path, ProjectConfigScript.compilationConfig, true)

    @JvmStatic
    fun loadPluginsScript(path: Path) = eval(path, PluginConfigScript.compilationConfig, true)

    @JvmStatic
    fun eval(path: Path, config: ScriptCompilationConfiguration, attached: Boolean): ResultWithDiagnostics<EvaluationResult>? {
        if (attached) {
            scriptFiles[path] = config
        }

        if (!path.exists()) {
            return null
        }

        val start = System.currentTimeMillis()
        val code = path.readText()
        val result = try {
            BasicJvmScriptingHost(ScriptingHostConfiguration {
                jvm {
                    baseClassLoader.put(CrucibleClassLoader)
                }
            }).eval(
                StringScriptSource(code, path.toString()),
                config,
                ScriptEvaluationConfiguration {
                    jvm {
                        baseClassLoader.put(CrucibleClassLoader)
                    }
                }
            )
        } catch (e: Exception) {
            throw ConfigScriptException("Error executing script '$path'", e)
        }

        result.reports.forEach {
            when (it.severity) {
                ScriptDiagnostic.Severity.INFO, ScriptDiagnostic.Severity.DEBUG -> debug(it.message)
                ScriptDiagnostic.Severity.WARNING -> warn(it.message)
                ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> throw ConfigScriptException(it)
            }
        }

        val returnValue = result.valueOrNull()?.returnValue

        if (returnValue is ResultValue.Error) {
            throw returnValue.error
        }

        debug("Loading config script $path took ${System.currentTimeMillis() - start} ms")

        return result
    }

    @JvmStatic
    fun start(args: Array<String>) {
        val startTime = System.currentTimeMillis()

        fallbackDependencyVersions.putAll(loadDependencyVersions(dependencyVersionsFile()))
        loadPluginsScript(pluginsScriptFile())
        loadScript(configScriptFile())

        info("> Finished config phase in ${System.currentTimeMillis() - startTime} ms")

        for (task in args) {
            Task.getTask(task).invoke()
        }
    }
}