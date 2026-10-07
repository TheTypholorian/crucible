package net.typho.crucible.task

import net.typho.crucible.LOG
import net.typho.crucible.deps.PrintClasspathTask
import net.typho.crucible.ide.RefreshIDETask
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JarTask

abstract class Task<R> : () -> R {
    companion object {
        @JvmField
        val all = mutableMapOf<String, Task<*>>(
            "compile" to CompileTask.Main,
            "jar" to JarTask.Main,
            "print_classpath" to PrintClasspathTask,
            "refresh_ide" to RefreshIDETask
        )

        @JvmStatic
        fun get(name: String): Task<*> = all[name] ?: throw NullPointerException("Task '$name' does not exist")

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Task<*>> get(name: String, type: Class<T>): T {
            val task = get(name)

            if (type.isAssignableFrom(task.javaClass)) {
                return task as T
            } else {
                throw ClassCastException("Task '$name' is not of type ${type.name}")
            }
        }
    }

    open val group: String
        get() = "other"
    open val description: String
        get() = ""

    override operator fun invoke(): R {
        LOG.info("> Executing task '$this'")
        return run()
    }

    protected abstract fun run(): R

    override fun toString(): String {
        return all.entries.firstOrNull { (key, value) -> value === this }?.key ?: super.toString()
    }

    abstract class RunOnce<R> : Task<R>() {
        private object Uninitialized

        private var value: Any? = Uninitialized

        @Suppress("UNCHECKED_CAST")
        final override fun invoke(): R {
            if (value !== Uninitialized) {
                LOG.debug("Using cached value of task '$this'")
                return value as R
            }

            return super.invoke().also { value = it }
        }
    }
}