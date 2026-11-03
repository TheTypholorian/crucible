package net.typho.crucible.deps

import net.typho.crucible.SimpleModule
import net.typho.crucible.ide_data.IDELibraryPathType
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.log.info
import kotlin.io.path.absolutePathString

class PrintClasspathTask(
    @JvmField
    val module: SimpleModule
) : Task<Unit>() {
    override val group: String
        get() = "debug"
    override val description: String
        get() = "Prints the dependencies for module '${module.id}'"

    override fun run() {
        val classpath = module.dependencies.getPaths(IDELibraryPathType.BINARY)
        info("Classpath:")
        classpath.forEach { path ->
            info("- ${path.absolutePathString()}")
        }
    }
}