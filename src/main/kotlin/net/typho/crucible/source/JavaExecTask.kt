package net.typho.crucible.source

import net.typho.crucible.LOG
import net.typho.crucible.deps.classpathString
import net.typho.crucible.error.JavaExecException
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.crucible.task.Task
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

open class JavaExecTask : Task<Unit>() {
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

    override fun run() {
        val process = ProcessBuilder(buildList {
            add("java")
            addAll(javaArgs().flatMap { listOf("-${it.first}", it.second) })
            add("-cp")
            add(classpath().classpathString())
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