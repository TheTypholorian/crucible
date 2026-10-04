package net.typho.crucible.ide

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.source.SourceSet
import net.typho.crucible.task.Task
import net.typho.data_util.impl.JsonFormat
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.writeText

object RefreshIDETask : Task<Unit> {
    val output by lazy { Crucible.projectCacheFolder.resolve("ide.json") }

    override fun invoke() {
        val data = CrucibleIDEData(
            Crucible.projectName,
            SourceSet.all.resolve().flatMap { set ->
                set.event.inputs.map { path ->
                    CrucibleIDEData.SourceSet(
                        set.event.type,
                        false, // TODO
                        path.absolutePathString()
                    )
                }
            },
            Crucible./* TODO */classpath.entries.map { path ->
                CrucibleIDEData.Dependency(path.name, listOf(CrucibleIDEData.Dependency.Path(
                    DependencyPathType.BINARY, // TODO
                    path.absolutePathString()
                )))
            }
        )
        output.parent.createDirectories()
        output.writeText(JsonFormat().write(CrucibleIDEData.CODEC, data))
    }
}