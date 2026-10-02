package net.typho.crucible.source

import net.typho.crucible.LOG
import net.typho.crucible.task.Task
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

object CompileTask : Task<Unit> {
    @OptIn(ExperimentalPathApi::class)
    override fun invoke() {
        SourceType.all.execute {
            LOG.debug("Compiling source type '${it.id}' to ${it.output.absolutePathString()}")
            it.output.deleteRecursively()
            it.output.createDirectories()
            it.compile()
        }
    }
}