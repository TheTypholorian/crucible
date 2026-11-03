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
import org.jetbrains.kotlin.idea.core.script.k2.ReloadScriptConfigurationService
import org.jetbrains.kotlin.psi.KtFile
import java.util.concurrent.CompletableFuture
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString

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
                val file = LocalFileSystem.getInstance().findFileByPath(path.toString())

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

        @Suppress("UNCHECKED_CAST")
        return info.project.toIntellij() as DataNode<ProjectData>
    }

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ) = false // TODO task cancelling
}