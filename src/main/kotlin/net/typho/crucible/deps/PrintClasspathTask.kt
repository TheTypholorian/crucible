package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.LOG
import net.typho.crucible.task.Task
import kotlin.io.path.absolutePathString

object PrintClasspathTask : Task<Unit> {
    override fun invoke() {
        LOG.info("Classpath:")
        Crucible.dependencies.classpath.entries.forEach { path ->
            LOG.info("- ${path.absolutePathString()}")
        }
    }
}