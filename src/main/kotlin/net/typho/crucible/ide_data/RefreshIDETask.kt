package net.typho.crucible.ide_data

import net.typho.crucible.Crucible
import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.task.InternalTask
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.data_util.json.JsonFormat
import java.io.ObjectOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

object RefreshIDETask : Task<Unit>(), InternalTask {
    override val group: String
        get() = "internal"
    override val description: String
        get() = "Outputs the ide info (project name, source sets, dependencies, and tasks) to ${Crucible.ideInfoFile}"

    override fun run() {
        val data = IDERootData(
            IDEProjectData(
                externalName = Crucible.name(),
                ideProjectFileDirectoryPath = CrucibleWrapper.projectRoot.resolve(".idea"),
                linkedExternalProjectPath = CrucibleWrapper.projectRoot,
                group = Crucible.group().ifEmpty { null },
                version = Crucible.version().ifEmpty { null },
                children = Crucible.modules.values.map { it.toIDE() } + tasks.entries.filter { (name, task) -> task !is InternalTask }
                    .map { (name, task) -> IDETaskData(
                        name = name,
                        description = task.description,
                        linkedExternalProjectPath = CrucibleWrapper.projectRoot,
                        group = task.group
                    ) }
            ),
            Crucible.loadedScripts.toList()
        )

        Crucible.ideInfoFile.parent.createDirectories()
        Crucible.ideInfoFile.writeText(JsonFormat.write(IDERootData.CODEC, data).asString())

        Crucible.scriptInfoFile.parent.createDirectories()
        ObjectOutputStream(Crucible.scriptInfoFile.outputStream()).use {
            it.writeObject(listOf(AbstractProjectConfig.createCompilationConfig(true)))
        }
    }
}