package net.typho.crucible

import net.typho.crucible.deps.DependencyCoordinates
import net.typho.crucible.deps.DependencyFinder
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    @JvmStatic
    fun main(args: Array<String>) {
        LOG.info("Starting crucible at ${Config.PROJECT_ROOT}")

        DependencyFinder.Maven("https://typho.net/maven", DependencyFinder.MAVEN_CENTRAL).use {
            println(it.find(DependencyCoordinates("net.typho:data_util:1.3.4")))
            println(it.find(DependencyCoordinates("net.typho:typho_publish:1.0.3")))
            println(it.find(DependencyCoordinates("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")))
        }

        LOG.info("Loading config script from ${Config.CONFIG_SCRIPT_FILE}")
        val result = BasicJvmScriptingHost().eval(Config.CONFIG_SCRIPT_FILE.toFile().toScriptSource(), ProjectConfig.ScriptConfig, ScriptEvaluationConfiguration())

        result.reports.forEach {
            when (it.severity) {
                ScriptDiagnostic.Severity.WARNING -> LOG.warn(it.message)
                ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> LOG.error(it.message)
                else -> {}
            }
        }

        /*
        val compiler = K2JVMCompiler()

        val args = compiler.createArguments().apply {
            freeArgs = listOf("test/Test.kt")
            destination = "test/out"
            jvmTarget = "21"
        }
        val messages = PrintingMessageCollector(
            System.err,
            MessageRenderer.PLAIN_FULL_PATHS,
            true
        )
        val result = compiler.exec(
            messages,
            Services.EMPTY,
            args
        )
        println("Compilation result: $result")
         */
    }
}