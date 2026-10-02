package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.ConfigScriptException
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.SourceType
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
    @JvmField
    var mainClass = "Main"

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
        LOG.debug("Starting crucible at ${Config.projectRoot}")

        loadConfigScript(Config.configScriptFile)

        Config.buildFolder.deleteRecursively()

        classpath += dependencies.map {
            repositories.find(it)?.path?.absolutePathString()
                ?: throw DependencyNotFoundException("Cannot find dependency $it, searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}")
        }
        LOG.debug("Class path: ${classpath.entries}")

        CompileTask() // TODO

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
            (classpath + SourceType.allOutputs).toString(),
            mainClass
            // program args
        )
        LOG.debug("Launching process, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()

        if (exitCode != 0) {
            LOG.error("Ended with exit code $exitCode")
        }
    }
}