package net.typho.crucible.ide

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.IDEModule
import net.typho.crucible.ide.data.IDEProject
import net.typho.crucible.ide.data.IDETask
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.source.SourceType
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
        val data = IDEProject(
            Crucible.name(),
            Crucible.modules.values.map { it.toIDE() },
            tasks.entries.filter { (name, task) -> task !is InternalTask }
                .map { (name, task) -> IDETask(name, task.group, task.description) },
            Crucible.loadedScripts.toList()
        )

        Crucible.ideInfoFile.parent.createDirectories()
        Crucible.ideInfoFile.writeText(JsonFormat.write(IDEProject.CODEC, data))

        Crucible.scriptInfoFile.parent.createDirectories()
        ObjectOutputStream(Crucible.scriptInfoFile.outputStream()).use {
            it.writeObject(listOf(AbstractProjectConfig.createCompilationConfig(true)))
        }
    }
}