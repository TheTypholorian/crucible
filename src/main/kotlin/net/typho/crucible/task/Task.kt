package net.typho.crucible.task

import net.typho.crucible.ide.RefreshIDETask
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.wrapper.log.debug
import net.typho.crucible.wrapper.log.info

abstract class Task<R> : PropertyContainer(), () -> R {
    companion object {
        @JvmField
        val tasks = mutableMapOf<String, Task<*>>(
            "refresh_ide" to RefreshIDETask
        )

        @JvmStatic
        fun getTask(id: String): Task<*> = tasks[id] ?: throw NullPointerException("Task '$id' does not exist")

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Task<*>> getTask(id: String, type: Class<T>): T {
            val task = getTask(id)

            if (type.isAssignableFrom(task.javaClass)) {
                return task as T
            } else {
                throw ClassCastException("Task '$id' is not of type ${type.name}")
            }
        }

        @JvmName("getTaskInlined")
        @JvmStatic
        inline fun <reified T : Task<*>> getTask(id: String): T {
            return getTask(id, T::class.java)
        }

        @JvmStatic
        fun <T : Task<*>> registerTask(id: String, task: T): T {
            tasks.put(id, task)?.let { old ->
                throw IllegalArgumentException("Task $old is already registered under the id '$id' (tried to register $task)")
            }

            return task
        }

        @JvmStatic
        fun <T : Task<*>> registerTask(id: String, task: T, config: T.() -> Unit) = registerTask(id, task).apply(config)

        @JvmStatic
        fun <T : Task<*>> registerTask(id: String, task: Class<T>): T {
            return registerTask(id, task.getConstructor().newInstance())
        }

        @JvmStatic
        fun <T : Task<*>> registerTask(id: String, task: Class<T>, config: T.() -> Unit): T {
            return registerTask(id, task).apply(config)
        }

        @JvmStatic
        inline fun <reified T : Task<*>> registerTask(id: String, noinline config: T.() -> Unit): T {
            return registerTask(id, T::class.java, config)
        }
    }

    open val group: String
        get() = "other"
    open val description: String
        get() = ""

    override operator fun invoke(): R {
        val start = System.currentTimeMillis()
        info("> Executing task '$this'")
        val result = run()
        info("> Finished task '$this' in ${System.currentTimeMillis() - start} ms")
        return result
    }

    protected abstract fun run(): R

    override fun toString(): String {
        return tasks.entries.firstOrNull { (key, value) -> value === this }?.key ?: super.toString()
    }

    abstract class RunOnce<R> : Task<R>() {
        private object Uninitialized

        private var value: Any? = Uninitialized

        @Suppress("UNCHECKED_CAST")
        final override fun invoke(): R {
            if (value !== Uninitialized) {
                debug("Using cached value of task '$this'")
                return value as R
            }

            return super.invoke().also { value = it }
        }
    }
}