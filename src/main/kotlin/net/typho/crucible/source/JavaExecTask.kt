package net.typho.crucible.source

import net.typho.crucible.deps.classpathString
import net.typho.crucible.error.JavaExecException
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.log.debug
import java.nio.file.Path

open class JavaExecTask : Task<Unit>() {
    override val group: String
        get() = "run"

    val classpath by listProperty<Path>().finalizeOnRead()
    val mainClass by property<String>().finalizeOnRead()
    val javaArgs by listProperty<Pair<String, String>>().finalizeOnRead()
    val programArgs by listProperty<String>().finalizeOnRead()

    override fun run() {
        val process = ProcessBuilder(buildList {
            add("java")
            addAll(javaArgs().flatMap { listOf("-${it.first}", it.second) })
            add("-cp")
            add(classpath().classpathString())
            add(mainClass())
            addAll(programArgs())
        })
        debug("Launching java process, command: ${process.command().joinToString(separator = " ") { if (it.any { it.isWhitespace() }) "\"$it\"" else it }}")
        val exitCode = process.inheritIO()
            .start()
            .waitFor()

        if (exitCode != 0) {
            throw JavaExecException("Ended with exit code $exitCode")
        }
    }
}