package net.typho.crucible.intellij.task

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import net.typho.crucible.intellij.CrucibleSystemManager
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

class CrucibleSystemTaskManager : ExternalSystemTaskManager<CrucibleSystemManager.ExecutionSettings> {
    override fun executeTasks(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: CrucibleSystemManager.ExecutionSettings,
        listener: ExternalSystemTaskNotificationListener
    ) {
        val command = ProcessBuilder(buildList {
            add("java")
            add("--sun-misc-unsafe-memory-access=allow")
            add("-jar")
            add(Path(projectPath).resolve("crucible.jar").absolutePathString())
            addAll(settings.tasks)
        })
            .directory(File(projectPath))
            .start()

        val stdOut = Thread {
            command.inputStream.reader().use { reader ->
                val buf = CharArray(4096)

                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    listener.onTaskOutput(id, String(buf, 0, n), ProcessOutputType.STDOUT)
                }
            }
        }
        val stdErr = Thread {
            command.errorStream.reader().use { reader ->
                val buf = CharArray(4096)

                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    listener.onTaskOutput(id, String(buf, 0, n), ProcessOutputType.STDERR)
                }
            }
        }

        stdOut.start()
        stdErr.start()

        val exitCode = command.waitFor()

        stdOut.join()
        stdErr.join()

        if (exitCode != 0) {
            listener.onFailure(projectPath, id, ExternalSystemException("Task failed with exit code $exitCode"))
        }
    }

    override fun cancelTask(
        id: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener
    ) = false // TODO
}