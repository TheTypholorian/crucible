package net.typho.crucible.intellij

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
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
import com.intellij.openapi.externalSystem.model.task.TaskData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.externalSystem.util.task.TaskExecutionSpec
import com.intellij.openapi.module.GeneralModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import kotlinx.coroutines.runBlocking
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.ide.data.SourceSetType
import org.jetbrains.kotlin.idea.core.script.k2.ReloadScriptConfigurationService
import org.jetbrains.kotlin.psi.KtFile
import java.util.concurrent.CompletableFuture
import kotlin.io.path.Path
import kotlin.io.path.absolute

class CrucibleProjectResolver : ExternalSystemProjectResolver<CrucibleSystemManager.ExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: CrucibleSystemManager.ExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val start = System.currentTimeMillis()
        val result = CompletableFuture<Boolean>()

        ExternalSystemUtil.runTask(
            TaskExecutionSpec.create()
                .withProject(id.project)
                .withSystemId(SYSTEM_ID)
                .withSettings(ExternalSystemTaskExecutionSettings().apply {
                    externalSystemIdString = SYSTEM_ID.id
                    externalProjectPath = projectPath
                    taskNames = listOf("refresh_ide")
                })
                .withExecutorId(DefaultRunExecutor.EXECUTOR_ID)
                .withProgressExecutionMode(ProgressExecutionMode.NO_PROGRESS_SYNC)
                .withCallback(result)
                .withListener(ForwardingTaskNotificationListener(id, listener))
                .navigateToError()
                .build()
        )

        if (result.join()) {
            listener.onTaskOutput(id, "\nRESOLVE FINISHED in ${System.currentTimeMillis() - start} ms", ProcessOutputType.STDOUT)
        } else {
            throw ExternalSystemException("Task failed")
        }

        val projectPath = Path(projectPath).absolute()
        val info = id.project.service<CrucibleSystemManager.LocalSettings>().state!!.loadInfo(projectPath)

        runBlocking {
            for (path in info.scriptFiles) {
                val file = LocalFileSystem.getInstance().findFileByPath(path)

                if (file == null) {
                    System.err.println("Supposed script file '$path' does not exist, skipping reload")
                    continue
                }

                val ktFile = readAction {
                    PsiManager.getInstance(id.project).findFile(file) as? KtFile
                }

                if (ktFile == null) {
                    System.err.println("Supposed script file '$file' is not a KtFile, skipping reload")
                    continue
                }

                ReloadScriptConfigurationService.getInstance(id.project).reloadScriptData(ktFile)
            }
        }

        return DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                SYSTEM_ID,
                info.projectName,
                projectPath.resolve(Project.DIRECTORY_STORE_FOLDER).toString(),
                projectPath.toString()
            ),
            null
        ).also { projectNode ->
            for (module in info.modules) {
                projectNode.createChild(
                    ProjectKeys.MODULE,
                    ModuleData(
                        module.name,
                        SYSTEM_ID,
                        GeneralModuleType.TYPE_ID,
                        module.name,
                        module.path,
                        module.path
                    )
                ).also { moduleNode ->
                    for (set in module.roots) {
                        moduleNode.createChild(
                            ProjectKeys.CONTENT_ROOT,
                            ContentRootData(SYSTEM_ID, set.path).also { sourceSetData ->
                                sourceSetData.storePath(when (set.type) {
                                    SourceSetType.CODE -> if (set.generated) ExternalSystemSourceType.SOURCE_GENERATED else ExternalSystemSourceType.SOURCE
                                    SourceSetType.RESOURCES -> if (set.generated) ExternalSystemSourceType.RESOURCE_GENERATED else ExternalSystemSourceType.RESOURCE
                                }, set.path)
                            }
                        )
                    }

                    for (dep in module.dependencies) {
                        moduleNode.createChild(
                            ProjectKeys.LIBRARY_DEPENDENCY,
                            LibraryDependencyData(
                                moduleNode.data,
                                LibraryData(
                                    SYSTEM_ID,
                                    dep.name
                                ).also { dependencyData ->
                                    for (path in dep.paths) {
                                        dependencyData.addPath(when (path.type) {
                                            DependencyPathType.BINARY -> LibraryPathType.BINARY
                                            DependencyPathType.SOURCE -> LibraryPathType.SOURCE
                                            DependencyPathType.DOC -> LibraryPathType.DOC
                                        }, path.path)
                                    }
                                },
                                LibraryLevel.MODULE
                            )
                        )
                    }
                }
            }

            for (task in info.tasks) {
                projectNode.createChild(
                    ProjectKeys.TASK,
                    TaskData(
                        SYSTEM_ID,
                        task.id,
                        projectPath.toString(),
                        task.description
                    ).also { taskData ->
                        taskData.group = task.group
                    }
                )
            }
        }
    }

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ) = false // TODO task cancelling
}