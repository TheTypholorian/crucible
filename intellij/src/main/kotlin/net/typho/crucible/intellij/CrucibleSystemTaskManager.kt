package net.typho.crucible.intellij

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import java.io.File

class CrucibleSystemTaskManager : ExternalSystemTaskManager<CrucibleSystemManager.ExecutionSettings> {
    override fun executeTasks(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: CrucibleSystemManager.ExecutionSettings,
        listener: ExternalSystemTaskNotificationListener
    ) {
        val command = ProcessBuilder(buildList {
            add("java")
            add("-jar")
            add("C:\\Users\\evan\\IdeaProjects\\crucible\\build\\libs\\crucible-1.0.0.jar")
            addAll(settings.tasks)
        })
            .directory(File(projectPath))
            .start()

        Thread {
            command.inputStream.reader().use { reader ->
                val buf = CharArray(4096)

                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    listener.onTaskOutput(id, String(buf, 0, n), ProcessOutputType.STDOUT)
                }
            }
        }.start()
        Thread {
            command.errorStream.reader().use { reader ->
                val buf = CharArray(4096)

                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    listener.onTaskOutput(id, String(buf, 0, n), ProcessOutputType.STDERR)
                }
            }
        }.start()

        val exitCode = command.waitFor()

        if (exitCode != 0) {
            throw RuntimeException(exitCode.toString())
        }
    }

    override fun cancelTask(
        id: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ) = false // TODO
}