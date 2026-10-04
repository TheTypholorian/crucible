package net.typho.crucible.task

import net.typho.crucible.deps.PrintClasspathTask
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JarTask

fun interface Task<R> : () -> R {
    companion object {
        @JvmField
        val STATIC = mutableMapOf<String, Task<*>>(
            "compile" to CompileTask,
            "jar" to JarTask.Main,
            "print_classpath" to PrintClasspathTask
        )

        @JvmStatic
        fun get(name: String): Task<*> = STATIC[name] ?: throw NullPointerException("Task '$name' does not exist")

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

    override operator fun invoke(): R

    abstract class RunOnce<R> : Task<R> {
        val value by lazy { invokeImpl() }

        final override fun invoke() = value

        protected abstract fun invokeImpl(): R
    }
}