package net.typho.crucible

import java.util.function.Supplier

open class Lazy<V : Any>(
    protected val supplier: Supplier<V>
) {
    protected var value: V? = null

    @JvmName("get")
    operator fun invoke(): V {
        value?.let { return it }
        val value = supplier.get()
        this.value = value
        return value
    }
}