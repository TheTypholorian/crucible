package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.error.ToolNotFoundException
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import javax.tools.Diagnostic
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies()
    @JvmField
    var mainClass = "Main"

    init {
        Thread.currentThread().uncaughtExceptionHandler = { thread, error ->
            if (Config.STACKTRACE) {
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
        LOG.debug("Starting crucible at ${Config.PROJECT_ROOT}")

        loadConfigScript(Config.CONFIG_SCRIPT_FILE)

        val compiler = K2JVMCompiler()

        val build = Config.PROJECT_ROOT.resolve("build")
        val buildClasses = build.resolve("classes")
        build.deleteRecursively() // TODO

        var classPath = Classpath(dependencies.mapTo(mutableSetOf()) {
            repositories.find(it)?.path?.absolutePathString()
                ?: throw DependencyNotFoundException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        })
        LOG.debug("Class path: ${classPath.entries}")

        val args = compiler.createArguments().apply {
            freeArgs = listOf(Config.PROJECT_ROOT.resolve("src/kotlin/").toString())
            javaSourceRoots = arrayOf(Config.PROJECT_ROOT.resolve("src/java/").toString())
            destination = buildClasses.toString()
            jvmTarget = "21"
            classpath = classPath.toString()
            noStdlib = true
        }
        val ktErrors = mutableListOf<String>()
        val messages = object : MessageCollector {
            var hasErrors = false

            override fun clear() {
            }

            override fun hasErrors() = hasErrors

            override fun report(
                severity: CompilerMessageSeverity,
                message: String,
                location: CompilerMessageSourceLocation?
            ) {
                val message = MessageRenderer.PLAIN_FULL_PATHS.render(severity, message, location)

                if (severity.isError) {
                    hasErrors = true
                    ktErrors.add(message)
                } else if (severity.isWarning) {
                    LOG.warn(message)
                } else {
                    LOG.debug(message)
                }
            }
        }
        val compileResult = compiler.exec(
            messages,
            Services.EMPTY,
            args
        )

        when (compileResult) {
            ExitCode.OK -> {}
            ExitCode.COMPILATION_ERROR, ExitCode.INTERNAL_ERROR, ExitCode.SCRIPT_EXECUTION_ERROR -> throw CompilationException(ktErrors.joinToString(separator = "\n").trim())
            ExitCode.OOM_ERROR -> throw OutOfMemoryError()
        }

        classPath += buildClasses

        val javaCompiler = ToolProvider.getSystemJavaCompiler() ?: throw ToolNotFoundException("Missing system java compiler, this usually means you are running with a JRE rather than a JDK.")
        val diagnostics = mutableListOf<Diagnostic<out JavaFileObject>>()
        val manager = javaCompiler.getStandardFileManager(diagnostics::add, null, null) // TODO diagnostics
        val files = manager.getJavaFileObjectsFromPaths(Config.PROJECT_ROOT.resolve("src/java/").walk().toList())
        val options = listOf(
            "-d", buildClasses.absolutePathString(),
            "-cp", classPath.toString(),
            "--release", "21"
        )
        val javaSuccess = javaCompiler.getTask(null, manager, diagnostics::add, options, null, files).call()

        if (!javaSuccess) {
            throw CompilationException(diagnostics.joinToString(separator = "\n").trim())
        }

        /*
        val projectJar = buildJars.resolve("project.jar")
        projectJar.parent.createDirectories()
        JarOutputStream(projectJar.outputStream()).use { jar ->
            buildClasses.walk().forEach { path ->
                if (path != buildClasses) {
                    val entry = path.relativeTo(buildClasses).toString().replace(File.separatorChar, '/')

                    if (path.toFile().isDirectory) {
                        jar.putNextEntry(ZipEntry("$entry/"))
                        jar.closeEntry()
                    } else {
                        jar.putNextEntry(ZipEntry(entry))
                        path.inputStream().use { it.transferTo(jar) }
                        jar.closeEntry()
                    }
                }
            }
        }
         */

        val process = ProcessBuilder(
            "java",
            "-cp",
            classPath.toString(),
            mainClass
            // program args
        )
        LOG.debug("Launching process, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()
        LOG.debug("Ended with code $exitCode")
    }
}