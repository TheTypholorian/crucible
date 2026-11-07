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
import net.typho.crucible.wrapper.PropertyManager
import net.typho.data_util.SerializedData
import net.typho.data_util.json.JsonFormat
import java.nio.file.Path
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

object Crucible : PropertyContainer("project") {
    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, exception ->
            if (exception is CompilationException || exception is ConfigScriptException) {
                error(exception.message)
            } else {
                exception.printStackTrace()
            }
        }
    }

    val group by property<String>().fallback("")
    val name by property<String>().fallback(CrucibleWrapper.projectRoot.name)
    val version by property<String>().fallback("")

    val javaVersion by property<String>().fallback("21")

    @JvmField
    val ideInfoFile = CrucibleWrapper.projectCacheFolder.resolve("ide.json")
    @JvmField
    val scriptInfoFile = CrucibleWrapper.projectCacheFolder.resolve("scripts.bin")
    val sourceInputFolder by property<Path>().fallback(CrucibleWrapper.projectRoot.resolve("src"))

    val buildFolder by property<Path>().fallback(CrucibleWrapper.projectRoot.resolve("build"))
    val pluginsScriptFile by property<Path>().fallback(CrucibleWrapper.projectRoot.resolve("plugins.crucible.kts"))
    val configScriptFile by property<Path>().fallback(CrucibleWrapper.projectRoot.resolve("crucible.kts"))
    val dependencyVersionsFile by property<Path>().fallback(CrucibleWrapper.projectRoot.resolve("deps.json"))

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

    @Suppress("UNUSED")
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