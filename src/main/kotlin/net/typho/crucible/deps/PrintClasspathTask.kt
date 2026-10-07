package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.LOG
import net.typho.crucible.task.Task
import kotlin.io.path.absolutePathString

object PrintClasspathTask : Task<Unit>() {
    override val group: String
        get() = "debug"
    override val description: String
        get() = "Prints a list of classpath elements (dependencies)"

    override fun run() {
        val classpath = Crucible.dependencies.paths
        LOG.info("Classpath:")
        classpath.forEach { path ->
            LOG.info("- ${path.absolutePathString()}")
        }
    }
}