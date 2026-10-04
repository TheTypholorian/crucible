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
import org.jetbrains.kotlin.idea.gradleJava.configuration.mpp.LibraryData
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.name

class CrucibleProjectResolver : ExternalSystemProjectResolver<CrucibleSystemManager.ExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: CrucibleSystemManager.ExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val projectPath = Paths.get(projectPath).absolute()
        return DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                SYSTEM_ID,
                projectPath.name, // TODO
                projectPath.resolve(".idea").toString(),
                projectPath.toString()
            ),
            null
        ).apply {
            addChild(DataNode(
                ProjectKeys.MODULE,
                ModuleData(
                    projectPath.name,
                    SYSTEM_ID,
                    GeneralModuleType.TYPE_ID,
                    projectPath.name,
                    projectPath.toString(),
                    projectPath.toString()
                ),
                this
            ).apply {
                addChild(DataNode(
                    ProjectKeys.CONTENT_ROOT,
                    ContentRootData(SYSTEM_ID, projectPath.toString()).apply {
                        storePath(ExternalSystemSourceType.SOURCE, "$projectPath/src/java")
                        storePath(ExternalSystemSourceType.SOURCE, "$projectPath/src/kotlin")
                        storePath(ExternalSystemSourceType.RESOURCE, "$projectPath/src/resources")
                    },
                    this
                ))
                addChild(DataNode(
                    ProjectKeys.LIBRARY_DEPENDENCY,
                    LibraryDependencyData(
                        data,
                        LibraryData(
                            SYSTEM_ID,
                            "kotlin-stdlib-2.4.0.jar"
                        ).apply {
                            addPath(LibraryPathType.BINARY, "C:\\Users\\evan\\.crucible\\caches\\maven\\org\\jetbrains\\kotlin\\kotlin-stdlib\\2.4.0\\kotlin-stdlib-2.4.0.jar")
                        },
                        LibraryLevel.PROJECT
                    ),
                    this
                ))
                addChild(DataNode(
                    ProjectKeys.LIBRARY_DEPENDENCY,
                    LibraryDependencyData(
                        data,
                        LibraryData(
                            SYSTEM_ID,
                            "data_util-1.3.5.jar"
                        ).apply {
                            addPath(LibraryPathType.BINARY, "C:\\Users\\evan\\.crucible\\caches\\maven\\net\\typho\\data_util\\1.3.5\\data_util-1.3.5.jar")
                        },
                        LibraryLevel.PROJECT
                    ),
                    this
                ))
            })
        }
    }

    //- C:\Users\evan\.crucible\caches\maven\org\jetbrains\kotlin\kotlin-stdlib\2.4.0\kotlin-stdlib-2.4.0.jar
    //- C:\Users\evan\.crucible\caches\maven\net\typho\data_util\1.3.5\data_util-1.3.5.jar

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ): Boolean {
        TODO()
    }
}