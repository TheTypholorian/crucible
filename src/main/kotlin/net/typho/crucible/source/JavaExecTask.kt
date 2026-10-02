package net.typho.crucible.source

import net.typho.crucible.Config
import net.typho.crucible.LOG
import net.typho.crucible.deps.Classpath
import net.typho.crucible.error.JavaExecException
import net.typho.crucible.task.Task

open class JavaExecTask(
    classpath: () -> Classpath,
    mainClass: () -> String,
    javaArgs: () -> List<Pair<String, String>> = { listOf() },
    programArgs: () -> List<String> = { listOf() }
) : Task<Unit> {
    val classPath by Config.finalizeOnRead(classpath)
    val mainClass by Config.finalizeOnRead(mainClass)
    val javaArgs by Config.finalizeOnRead(javaArgs)
    val programArgs by Config.finalizeOnRead(programArgs)

    override fun invoke() {
        val process = ProcessBuilder(
            listOf("java") + (javaArgs + ("cp" to classPath.toString())).flatMap { listOf("-${it.first}", it.second) } + listOf(mainClass) + programArgs
        )
        LOG.debug("Launching java process, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()

        if (exitCode != 0) {
            throw JavaExecException("Ended with exit code $exitCode")
        }
    }
}