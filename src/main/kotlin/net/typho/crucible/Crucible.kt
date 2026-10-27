package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.log.*
import net.typho.data_util.impl.JsonFormat
import org.jetbrains.kotlin.ir.types.IdSignatureValues.result
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
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

        System.getProperty("crucible.config_files")?.split(File.pathSeparatorChar)?.forEach { CrucibleWrapper.loadConfig(Path(it), true) }
    }

    @JvmField
    val projectRoot = CrucibleWrapper.projectRoot
    @JvmField
    val projectCacheFolder = projectRoot.resolve(".crucible")

    val group by lazyProperty { System.getProperty("crucible.project_group") ?: "" }.finalizeOnRead()
    val name by lazyProperty { System.getProperty("crucible.project_name") ?: projectRoot.name }.finalizeOnRead()
    val version by lazyProperty { System.getProperty("crucible.project_version") ?: "" }.finalizeOnRead()

    val javaVersion by lazyProperty { System.getProperty("crucible.java_version") ?: "21" }.finalizeOnRead()

    @JvmField
    val ideInfoFile = projectCacheFolder.resolve("ide.json")
    @JvmField
    val scriptInfoFile = projectCacheFolder.resolve("scripts.bin")
    val sourceInputFolder by lazyProperty { projectRoot.resolve("src") }.finalizeOnRead()

    val buildFolder by lazyProperty { System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: projectRoot.resolve("build") }.finalizeOnRead()
    val configScriptFile by lazyProperty { System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: projectRoot.resolve("crucible.kts") }.finalizeOnRead()
    val dependencyVersionsFile by lazyProperty { System.getProperty("crucible.dependency_versions_file")?.let { Path(it).absolute() } ?: projectRoot.resolve("deps.json") }.finalizeOnRead()

    @JvmField
    val repositories = Repositories()
    @JvmField
    val modules = NamedContainer { Module(it) }

    @JvmField
    val fallbackDependencyVersions = mutableMapOf<String, String>()

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadDependencyVersions(path: Path): Map<String, String> {
        return if (path.exists()) {
            JsonFormat.read(path.readText()) as Map<String, String>
        } else {
            mapOf()
        }
    }

    internal val loadedScripts = mutableSetOf<String>()

    @JvmStatic
    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    @JvmStatic
    fun loadConfigScript(path: Path) {
        if (path.exists()) {
            loadConfigScript(path.readText(), path.absolutePathString())
        }
    }

    @JvmStatic
    fun loadConfigScript(code: String, path: String) {
        debug("Loading config script $path")
        loadedScripts.add(path)
        val result = try {
            BasicJvmScriptingHost(ScriptingHostConfiguration {
                jvm {
                    baseClassLoader.put(CrucibleClassLoader)
                }
            }).eval(
                StringScriptSource(code, path),
                AbstractProjectConfig.createCompilationConfig(),
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
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()

        fallbackDependencyVersions.putAll(loadDependencyVersions(dependencyVersionsFile()))
        loadConfigScript(configScriptFile())

        info("> Finished config phase in ${System.currentTimeMillis() - startTime} ms")

        for (task in args) {
            Task.getTask(task).invoke()
        }
    }
}