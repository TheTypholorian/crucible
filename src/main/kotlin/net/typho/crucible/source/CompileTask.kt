package net.typho.crucible.source

import net.typho.crucible.task.Task
import java.nio.file.Path

open class CompileTask : Task.RunOnce<List<Path>>() {
    override val group: String
        get() = "build"

    val sourceTypes by listProperty<SourceType>().finalizeOnRead()

    override fun run(): List<Path> {
        return sourceTypes().fold(listOf<SourceType>()) { previous, set ->
            set.invoke(sourceTypes(), previous)
            previous + set
        }.map { it.output() }
    }
}