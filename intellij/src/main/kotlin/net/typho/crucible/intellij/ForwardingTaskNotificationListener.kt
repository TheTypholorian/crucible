package net.typho.crucible.intellij

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemTaskExecutionEvent
import java.lang.Exception

class ForwardingTaskNotificationListener(
    @JvmField
    val newId: ExternalSystemTaskId,
    @JvmField
    val parent: ExternalSystemTaskNotificationListener
) : ExternalSystemTaskNotificationListener {
    override fun onStart(
        projectPath: String,
        id: ExternalSystemTaskId
    ) = parent.onStart(projectPath, newId)

    override fun onStart(
        id: ExternalSystemTaskId,
        workingDir: String?
    ) = parent.onStart(newId, workingDir)

    override fun onStart(id: ExternalSystemTaskId) = parent.onStart(newId)

    override fun onSuccess(
        projectPath: String,
        id: ExternalSystemTaskId
    ) = parent.onSuccess(projectPath, newId)

    override fun onSuccess(id: ExternalSystemTaskId) = parent.onSuccess(newId)

    override fun onFailure(
        projectPath: String,
        id: ExternalSystemTaskId,
        exception: Exception
    ) = parent.onFailure(projectPath, newId, exception)

    override fun onFailure(
        id: ExternalSystemTaskId,
        e: Exception
    ) = parent.onFailure(newId, e)

    override fun onCancel(
        projectPath: String,
        id: ExternalSystemTaskId
    ) = parent.onCancel(projectPath, newId)

    override fun onCancel(id: ExternalSystemTaskId) = parent.onCancel(newId)

    override fun onEnd(
        projectPath: String,
        id: ExternalSystemTaskId
    ) = parent.onEnd(projectPath, newId)

    override fun onEnd(id: ExternalSystemTaskId) = parent.onEnd(newId)

    override fun onEnvironmentPrepared(id: ExternalSystemTaskId) = parent.onEnvironmentPrepared(newId)

    override fun onStatusChange(event: ExternalSystemTaskNotificationEvent) = parent.onStatusChange(when (event) {
        is ExternalSystemBuildEvent -> ExternalSystemBuildEvent(newId, event.buildEvent)
        is ExternalSystemTaskExecutionEvent -> ExternalSystemTaskExecutionEvent(newId, event.progressEvent)
        else -> ExternalSystemTaskNotificationEvent(newId, event.description)
    })

    override fun onTaskOutput(
        id: ExternalSystemTaskId,
        text: String,
        outputType: ProcessOutputType
    ) = parent.onTaskOutput(newId, text, outputType)

    override fun onTaskOutput(
        id: ExternalSystemTaskId,
        text: String,
        stdOut: Boolean
    ) = parent.onTaskOutput(newId, text, stdOut)

    override fun beforeCancel(id: ExternalSystemTaskId) = parent.beforeCancel(newId)
}