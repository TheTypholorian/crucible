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
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.relativeToOrNull
import kotlin.io.path.walk
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Crucible {
    @JvmField
    val repositories = mutableListOf<Repository>()
    @JvmField
    val dependencies = mutableListOf<DependencyCoordinates>()
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
                    ScriptDiagnostic.Severity.WARNING -> LOG.warn(it.message)
                    ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> LOG.error(it.message)
                    else -> {}
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
        val buildJars = build.resolve("jars")
        build.deleteRecursively() // TODO

        val classPath = Classpath(dependencies.map {
            repositories.find(it)?.absolutePathString()
                ?: throw NullPointerException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        })

        val args = compiler.createArguments().apply {
            freeArgs = listOf(Config.PROJECT_ROOT.resolve("src/main/kotlin/").toString())
            destination = buildClasses.toString()
            jvmTarget = "21"
            classpath = classPath.toString()
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
        LOG.debug("Compilation result: $compileResult")

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

        val process = ProcessBuilder(
            "java",
            "-cp",
            (classPath + projectJar).toString(),
            mainClass
            // program args
        )
        LOG.debug("Launching jar, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()
        LOG.debug("Ended with code $exitCode")
    }
}