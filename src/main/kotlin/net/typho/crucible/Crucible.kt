package net.typho.crucible

import net.typho.crucible.deps.Classpath
import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.script.ProjectConfigScript
import net.typho.crucible.task.Task
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies()
    @JvmStatic
    var classpath = Classpath()

    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, error ->
            if (Config.stacktrace) {
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
            LOG.debug("Loading config script from $path")
            val result = BasicJvmScriptingHost().eval(
                StringScriptSource(path.readText(), path.absolutePathString()),
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
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        LOG.debug("Starting crucible at ${Config.projectRoot}")

        loadConfigScript(Config.configScriptFile)

        Config.buildFolder.deleteRecursively()

        classpath += dependencies.map {
            repositories.find(it)?.path
                ?: throw DependencyNotFoundException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        }
        LOG.debug("Class path: ${classpath.entries}")
        LOG.info("Finished config phase in ${(System.currentTimeMillis() - startTime) / 1000f} seconds")

        for (task in args) {
            LOG.info("Executing task '$task'")
            Task.get(task).invoke()
        }
    }
}