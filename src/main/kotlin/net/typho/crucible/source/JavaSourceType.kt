package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.error.CompilationException
import net.typho.crucible.error.ToolNotFoundException
import javax.tools.Diagnostic
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.io.path.absolutePathString
import kotlin.io.path.walk

object JavaSourceType : SourceType() {
    override val id = "java"
    override val type = Type.CODE

    override fun compile() {
        val javaCompiler = ToolProvider.getSystemJavaCompiler() ?: throw ToolNotFoundException("Missing system java compiler, this usually means you are running with a JRE rather than a JDK.")
        val diagnostics = mutableListOf<Diagnostic<out JavaFileObject>>()
        val manager = javaCompiler.getStandardFileManager(diagnostics::add, null, null)
        val files = manager.getJavaFileObjectsFromPaths(inputs.flatMap { it.walk() })
        val options = listOf(
            "-d", output.absolutePathString(),
            "-cp", (Crucible.classpath + KotlinSourceType.output).toString(),
            "--release", "21"
        )
        val javaSuccess = javaCompiler.getTask(null, manager, diagnostics::add, options, null, files).call()

        if (!javaSuccess) {
            throw CompilationException(diagnostics.joinToString(separator = "\n").trim())
        }
    }
}