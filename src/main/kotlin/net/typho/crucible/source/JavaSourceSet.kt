package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.deps.classpathString
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ToolNotFoundException
import net.typho.crucible.ide.data.SourceSetType
import javax.tools.Diagnostic
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.walk

open class JavaSourceSet : SourceSet() {
    override val id = "java"
    override val type = SourceSetType.CODE

    @OptIn(ExperimentalPathApi::class)
    override fun compile(all: List<SourceSet>, previous: List<SourceSet>) {
        output().deleteRecursively()
        output().createDirectories()

        val javaCompiler = ToolProvider.getSystemJavaCompiler() ?: throw ToolNotFoundException("Missing system java compiler, this usually means you are running with a JRE rather than a JDK.")
        val diagnostics = mutableListOf<Diagnostic<out JavaFileObject>>()
        val manager = javaCompiler.getStandardFileManager(diagnostics::add, null, null)
        val files = manager.getJavaFileObjectsFromPaths(inputs.flatMap { it.walk() })
        val options = listOf(
            "-d", output().absolutePathString(),
            "-cp", (Crucible.dependencies.paths + previous.map { it.output() }).classpathString(),
            "--release", Crucible.javaVersion()
        )
        val success = javaCompiler.getTask(null, manager, diagnostics::add, options, null, files).call()

        if (!success) {
            throw CompilationException(diagnostics.joinToString(separator = "\n").trim())
        }
    }

    object Main : JavaSourceSet()
}