package net.typho.crucible.task

import net.typho.crucible.source.CompileTask

fun interface Task<R> {
    companion object {
        @JvmField
        val STATIC = mutableMapOf<String, Task<*>>(
            "compile" to CompileTask
        )
    }

    operator fun invoke(): R
}