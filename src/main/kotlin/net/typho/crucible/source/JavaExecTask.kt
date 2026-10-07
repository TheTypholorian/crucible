package net.typho.crucible.source

import net.typho.crucible.LOG
import net.typho.crucible.deps.Classpath
import net.typho.crucible.error.JavaExecException
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.crucible.task.Task
import java.nio.file.Path

open class JavaExecTask : Task<Unit> {
    override val group: String
        get() = "run"

    @JvmField
    val classpath = ListProperty<Path>().finalizeOnRead()
    @JvmField
    val mainClass = Property<String>().finalizeOnRead()
    @JvmField
    val javaArgs = ListProperty<Pair<String, String>>().finalizeOnRead()
    @JvmField
    val programArgs = ListProperty<String>().finalizeOnRead()

    override fun invoke() {
        val process = ProcessBuilder(buildList {
            add("java")
            addAll((javaArgs() + ("cp" to Classpath(classpath()).toString())).flatMap { listOf("-${it.first}", it.second) })
            add(mainClass())
            addAll(programArgs())
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