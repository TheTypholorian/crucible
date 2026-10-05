package net.typho.crucible.source

import net.typho.crucible.Crucible
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
    override val group: String
        get() = "run"

    val classPath by Crucible.finalizeOnRead(classpath)
    val mainClass by Crucible.finalizeOnRead(mainClass)
    val javaArgs by Crucible.finalizeOnRead(javaArgs)
    val programArgs by Crucible.finalizeOnRead(programArgs)

    override fun invoke() {
        val process = ProcessBuilder(buildList {
            add("java")
            addAll((javaArgs + ("cp" to classPath.toString())).flatMap { listOf("-${it.first}", it.second) })
            add(mainClass)
            addAll(programArgs)
        })
        LOG.debug("Launching java process, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()

        if (exitCode != 0) {
            throw JavaExecException("Ended with exit code $exitCode")
        }
    }
}