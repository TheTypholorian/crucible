package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.error.ToolNotFoundException
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import javax.tools.ToolProvider
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
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

        val classPath = Classpath(dependencies.mapTo(mutableSetOf()) {
            repositories.find(it)?.path?.absolutePathString()
                ?: throw DependencyNotFoundException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        }) + buildClasses
        LOG.debug("Class path: ${classPath.entries}")

        val args = compiler.createArguments().apply {
            freeArgs = listOf(Config.PROJECT_ROOT.resolve("src/kotlin/").toString())
            javaSourceRoots = arrayOf(Config.PROJECT_ROOT.resolve("src/java/").toString())
            destination = buildClasses.toString()
            jvmTarget = "21"
            classpath = classPath.toString()
            noStdlib = true
        }
        val errorStream = ByteArrayOutputStream()
        val messages = PrintingMessageCollector(
            PrintStream(errorStream),
            MessageRenderer.PLAIN_FULL_PATHS,
            Config.DEBUG
        )
        val compileResult = compiler.exec(
            messages,
            Services.EMPTY,
            args
        )

        when (compileResult) {
            ExitCode.OK -> {}
            ExitCode.COMPILATION_ERROR, ExitCode.INTERNAL_ERROR, ExitCode.SCRIPT_EXECUTION_ERROR -> throw CompilationException(errorStream.toString().trim())
            ExitCode.OOM_ERROR -> throw OutOfMemoryError()
        }

        val javaCompiler = ToolProvider.getSystemJavaCompiler() ?: throw ToolNotFoundException("Missing system java compiler, this usually means you are running with a JRE rather than a JDK.")
        val manager = javaCompiler.getStandardFileManager(null, null, null) // TODO diagnostics
        val files = manager.getJavaFileObjectsFromPaths(Config.PROJECT_ROOT.resolve("src/java/").walk().toList())
        val options = listOf(
            "-d", buildClasses.absolutePathString(),
            "-cp", classPath.toString()
        )
        val javaSuccess = javaCompiler.getTask(null, manager, null, options, null, files).call()

        if (!javaSuccess) {
            TODO()
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