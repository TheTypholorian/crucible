package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.LOG
import net.typho.crucible.deps.classpathString
import net.typho.crucible.error.CompilationException
import net.typho.crucible.ide.data.SourceSetType
import net.typho.misc_util.EventGraph
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

open class KotlinSourceSet : SourceSet() {
    override val id = "kotlin"
    override val type = SourceSetType.CODE

    override fun postRegister(event: EventGraph<String, *>.Event) {
        event.before(JavaSourceSet.Main)
    }

    @OptIn(ExperimentalPathApi::class)
    override fun compile(all: List<SourceSet>, previous: List<SourceSet>) {
        output().deleteRecursively()
        output().createDirectories()

        val compiler = K2JVMCompiler()
        val args = compiler.createArguments().apply {
            freeArgs = inputs().map { it.absolutePathString() }
            javaSourceRoots = all.filterIsInstance<JavaSourceSet>()
                .flatMap { it.inputs() }
                .map { it.absolutePathString() }
                .toTypedArray()
            destination = output().absolutePathString()
            jvmTarget = Crucible.javaVersion()
            classpath = Crucible.dependencies.paths.classpathString()
            noStdlib = true
        }
        val errors = mutableListOf<String>()
        val compileResult = compiler.exec(
            object : MessageCollector {
                override fun clear() {
                }

                override fun hasErrors() = errors.isNotEmpty()

                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?
                ) {
                    val message = MessageRenderer.PLAIN_FULL_PATHS.render(severity, message, location)

                    if (severity.isError) {
                        errors.add(message)
                    } else if (severity.isWarning) {
                        LOG.warn(message)
                    } else {
                        LOG.debug(message)
                    }
                }
            },
            Services.EMPTY,
            args
        )

        when (compileResult) {
            ExitCode.OK -> {}
            ExitCode.COMPILATION_ERROR, ExitCode.INTERNAL_ERROR, ExitCode.SCRIPT_EXECUTION_ERROR -> throw CompilationException(errors.joinToString(separator = "\n").trim())
            ExitCode.OOM_ERROR -> throw OutOfMemoryError()
        }
    }

    object Main : KotlinSourceSet()
}