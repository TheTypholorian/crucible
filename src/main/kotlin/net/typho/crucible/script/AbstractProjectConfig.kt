package net.typho.crucible.script

import net.typho.crucible.Crucible
import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.task.Task

abstract class AbstractProjectConfig {
    val repositories by Crucible::repositories
    val dependencies by Crucible::dependencies

    var group by Crucible.projectGroup
    var name by Crucible.projectName
    var version by Crucible.projectVersion

    var javaVersion by Crucible.javaVersion

    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    fun dependencies(action: Dependencies.() -> Unit) = action(dependencies)

    fun getTask(name: String) = Task.get(name)

    fun <T : Task<*>> getTask(name: String, type: Class<T>) = Task.get(name, type)

    fun <T : Task<*>> registerTask(name: String, task: T): T {
        Task.STATIC.put(name, task)?.let { old ->
            throw IllegalArgumentException("Task $old is already registered under the name '$name' (tried to register $task)")
        }

        return task
    }

    fun <T : Task<*>> registerTask(name: String, task: Class<T>, config: T.() -> Unit): T {
        val task = task.getConstructor().newInstance()
        config(task)
        return registerTask(name, task)
    }

    inline fun <reified T : Task<*>> registerTask(name: String, noinline config: T.() -> Unit): T {
        return registerTask(name, T::class.java, config)
    }
}