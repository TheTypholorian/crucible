package net.typho.crucible

import net.typho.crucible.Crucible.buildFolder
import net.typho.crucible.Crucible.configScriptFile
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
    val sourceInputFolder by lazyProperty { projectRoot.resolve("src") }

    val buildFolder by lazyProperty { System.getProperty("crucible.build_folder")?.let { Path(it).absolute() } ?: projectRoot.resolve("build") }.finalizeOnRead()
    val jarOutputFolder by lazyProperty { buildFolder().resolve("jars") }.finalizeOnRead()
    val sourceOutputFolder by lazyProperty { buildFolder().resolve("src") }.finalizeOnRead()
    val configScriptFile by lazyProperty { System.getProperty("crucible.config_script")?.let { Path(it).absolute() } ?: projectRoot.resolve("crucible.kts") }.finalizeOnRead()

    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies(repositories)

    internal val loadedScripts = mutableSetOf<String>()

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
        val result = BasicJvmScriptingHost(ScriptingHostConfiguration {
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