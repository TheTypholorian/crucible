package net.typho.crucible

import net.typho.crucible.deps.DependencyCoordinates
import net.typho.crucible.deps.Repository
import net.typho.crucible.deps.Repository.Companion.find
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    @JvmField
    val repositories = mutableListOf<Repository>()
    @JvmField
    val dependencies = mutableListOf<DependencyCoordinates>()

    @JvmStatic
    fun loadConfigScript(path: Path) {
        if (path.exists()) {
            LOG.debug("Loading config script from $path")
            val result = BasicJvmScriptingHost().eval(
                StringScriptSource(path.readText(), path.absolutePathString()),
                ProjectConfig.ScriptConfig,
                ScriptEvaluationConfiguration()
            )

            result.reports.forEach {
                when (it.severity) {
                    ScriptDiagnostic.Severity.WARNING -> LOG.warn(it.message)
                    ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> LOG.error(it.message)
                    else -> {}
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        LOG.debug("Starting crucible at ${Config.PROJECT_ROOT}")

        loadConfigScript(Config.CONFIG_SCRIPT_FILE)

        val compiler = K2JVMCompiler()

        val args = compiler.createArguments().apply {
            freeArgs = listOf(Config.PROJECT_ROOT.resolve("src", "main", "kotlin", "Test.kt").toString())
            destination = Config.PROJECT_ROOT.resolve("out").toString()
            jvmTarget = "21"
            classpath = dependencies.joinToString(separator = File.pathSeparator) {
                repositories.find(it)?.absolutePathString()
                    ?: throw NullPointerException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
            }
            noStdlib = true
        }
        val messages = PrintingMessageCollector(
            System.err,
            MessageRenderer.PLAIN_FULL_PATHS,
            Config.DEBUG
        )
        val compileResult = compiler.exec(
            messages,
            Services.EMPTY,
            args
        )
        LOG.info("Compilation result: $compileResult")
    }
}