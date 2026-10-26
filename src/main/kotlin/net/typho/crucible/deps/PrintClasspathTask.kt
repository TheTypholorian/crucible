package net.typho.crucible.deps

import net.typho.crucible.Module
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.log.info
import kotlin.io.path.absolutePathString

class PrintClasspathTask(
    @JvmField
    val module: Module
) : Task<Unit>() {
    override val group: String
        get() = "debug"
    override val description: String
        get() = "Prints the dependencies for module '${module.id}'"

    override fun run() {
        val classpath = module.dependencies.getPaths(DependencyPathType.BINARY)
        info("Classpath:")
        classpath.forEach { path ->
            info("- ${path.absolutePathString()}")
        }
    }
}