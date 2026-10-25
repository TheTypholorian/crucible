package net.typho.crucible.source

import net.typho.crucible.task.Task
import java.nio.file.Path

open class CompileTask : Task.RunOnce<List<Path>>() {
    override val group: String
        get() = "build"

    val sourceSets by listProperty<SourceSet>().finalizeOnRead()

    override fun run(): List<Path> {
        return sourceSets().fold(listOf<SourceSet>()) { previous, set ->
            set.invoke(sourceSets(), previous)
            previous + set
        }.map { it.output() }
    }

    object Main : CompileTask() {
        override val description: String
            get() = "Compiles all source sets and moves their contents into the build folder"

        init {
            sourceSets.addAll { SourceSet.all.resolve().map { it.event } }
        }
    }
}