package net.typho.crucible.ide

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.source.SourceSet
import net.typho.crucible.task.InternalTask
import net.typho.crucible.task.Task
import net.typho.data_util.impl.JsonFormat
import java.io.ObjectOutputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

object RefreshIDETask : Task<Unit>(), InternalTask {
    override val group: String
        get() = "internal"
    override val description: String
        get() = "Outputs the ide info (project name, source sets, dependencies, and tasks) to ${Crucible.ideInfoFile}"

    override fun run() {
        val data = CrucibleIDEData(
            Crucible.name(),
            listOf(CrucibleIDEData.Module( // TODO modules
                Crucible.name(),
                Crucible.projectRoot.absolutePathString(),
                SourceSet.all.resolve().flatMap { set ->
                    set.event.inputs().map { path ->
                        CrucibleIDEData.SourceSet(
                            set.event.type,
                            Crucible.dependencies, // TODO
                            false, // TODO
                            path.absolutePathString()
                        )
                    }
                },
                Task.all.entries.filter { (name, task) -> task !is InternalTask }.map { (name, task) -> CrucibleIDEData.Task(name, task.group, task.description) }
            )),
            Crucible.loadedScripts.toList()
        )

        Crucible.ideInfoFile.parent.createDirectories()
        Crucible.ideInfoFile.writeText(JsonFormat().write(CrucibleIDEData.CODEC, data))

        Crucible.scriptInfoFile.parent.createDirectories()
        ObjectOutputStream(Crucible.scriptInfoFile.outputStream()).use {
            it.writeObject(listOf(AbstractProjectConfig.createCompilationConfig(true)))
        }
    }
}