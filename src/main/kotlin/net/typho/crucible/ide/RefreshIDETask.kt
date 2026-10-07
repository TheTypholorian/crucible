package net.typho.crucible.ide

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.source.SourceSet
import net.typho.crucible.task.Task
import net.typho.data_util.impl.JsonFormat
import java.io.ObjectOutputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

object RefreshIDETask : Task<Unit>() {
    override val group: String
        get() = "internal"
    override val description: String
        get() = "Outputs the ide info (project name, source sets, dependencies, and tasks) to ${Crucible.ideInfoFile}"

    override fun run() {
        val data = CrucibleIDEData(
            Crucible.projectName(),
            SourceSet.all.resolve().flatMap { set ->
                set.event.inputs().map { path ->
                    CrucibleIDEData.SourceSet(
                        set.event.type,
                        false, // TODO
                        path.absolutePathString()
                    )
                }
            },
            Crucible.dependencies(),
            Task.all.entries.filter { it.value.group != "internal" }.map { (name, task) -> CrucibleIDEData.Task(name, task.group, task.description) }
        )

        Crucible.ideInfoFile.parent.createDirectories()
        Crucible.ideInfoFile.writeText(JsonFormat().write(CrucibleIDEData.CODEC, data))

        Crucible.scriptInfoFile.parent.createDirectories()
        ObjectOutputStream(Crucible.scriptInfoFile.outputStream()).use {
            it.writeObject(listOf(AbstractProjectConfig.COMP_CONFIG))
        }
    }
}