package net.typho.crucible.source

import net.typho.crucible.LOG
import net.typho.crucible.task.Task
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

object CompileTask : Task.RunOnce<List<Path>>() {
    override val group: String
        get() = "build"
    override val description: String
        get() = "Compiles all source sets and moves their contents into the build folder"

    @OptIn(ExperimentalPathApi::class)
    override fun invokeImpl(): List<Path> {
        return SourceSet.all.resolve().map {
            LOG.debug("Compiling source type '${it.id}' to ${it.event.output.absolutePathString()}")
            it.event.output.deleteRecursively()
            it.event.output.createDirectories()
            it.event.compile()
            it.event.output
        }
    }
}