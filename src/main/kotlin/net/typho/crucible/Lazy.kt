package net.typho.crucible

open class Lazy<V : Any>(
    protected val supplier: () -> V
) {
    protected var value: V? = null

    @JvmName("get")
    operator fun invoke(): V {
        value?.let { return it }
        val value = supplier()
        this.value = value
        return value
    }
}