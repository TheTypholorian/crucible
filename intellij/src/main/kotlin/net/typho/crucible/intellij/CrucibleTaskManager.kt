package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager

class CrucibleTaskManager : ExternalSystemTaskManager<CrucibleTaskManager.ExecutionSettings> {
    @Deprecated("Deprecated in Java")
    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: List<String>,
        projectPath: String,
        settings: ExecutionSettings?,
        vmOptions: List<String>,
        scriptParameters: List<String>,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelTask(
        id: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ): Boolean {
        TODO("Not yet implemented")
    }

    class ExecutionSettings : ExternalSystemExecutionSettings()
}