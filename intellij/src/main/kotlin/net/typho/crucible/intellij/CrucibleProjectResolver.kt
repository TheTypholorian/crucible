package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.ExternalSystemSourceType
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.model.project.LibraryDependencyData
import com.intellij.openapi.externalSystem.model.project.LibraryLevel
import com.intellij.openapi.externalSystem.model.project.LibraryPathType
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.GeneralModuleType
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.ide.data.SourceSetType
import net.typho.data_util.impl.JsonFormat
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.readText

class CrucibleProjectResolver : ExternalSystemProjectResolver<CrucibleSystemManager.ExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: CrucibleSystemManager.ExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val projectPath = Paths.get(projectPath).absolute()
        val infoPath = projectPath.resolve(".crucible").resolve("ide.json")
        val info = JsonFormat().read(CrucibleIDEData.CODEC, infoPath.readText())

        return DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                SYSTEM_ID,
                info.projectName,
                projectPath.resolve(".idea").toString(),
                projectPath.toString()
            ),
            null
        ).apply {
            addChild(DataNode(
                ProjectKeys.MODULE,
                ModuleData(
                    info.projectName,
                    SYSTEM_ID,
                    GeneralModuleType.TYPE_ID,
                    info.projectName,
                    projectPath.toString(),
                    projectPath.toString()
                ),
                this
            ).apply {
                addChild(DataNode(
                    ProjectKeys.CONTENT_ROOT,
                    ContentRootData(SYSTEM_ID, projectPath.toString()).apply {
                        for (set in info.sourceSets) {
                            storePath(when (set.type) {
                                SourceSetType.CODE -> if (set.generated) ExternalSystemSourceType.SOURCE_GENERATED else ExternalSystemSourceType.SOURCE
                                SourceSetType.RESOURCES -> if (set.generated) ExternalSystemSourceType.RESOURCE_GENERATED else ExternalSystemSourceType.RESOURCE
                            }, set.path)
                        }
                    },
                    this
                ))

                for (dep in info.dependencies) {
                    addChild(DataNode(
                        ProjectKeys.LIBRARY_DEPENDENCY,
                        LibraryDependencyData(
                            data,
                            LibraryData(
                                SYSTEM_ID,
                                dep.name
                            ).apply {
                                for (path in dep.paths) {
                                    addPath(when (path.type) {
                                        DependencyPathType.BINARY -> LibraryPathType.BINARY
                                        DependencyPathType.SOURCE -> LibraryPathType.SOURCE
                                        DependencyPathType.DOC -> LibraryPathType.DOC
                                    }, path.path)
                                }
                            },
                            LibraryLevel.PROJECT
                        ),
                        this
                    ))
                }
            })
        }
    }

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ): Boolean {
        TODO()
    }
}