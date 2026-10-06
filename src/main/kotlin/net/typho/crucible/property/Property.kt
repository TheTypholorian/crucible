package net.typho.crucible.property

import java.util.function.Function
import java.util.function.UnaryOperator

open class Property<V : Any> : () -> V {
    protected enum class Finalization {
        NONE,
        ENABLED,
        FINAL
    }

    @JvmField
    protected var finalization = Finalization.NONE
    @JvmField
    protected val supplier: (() -> V)?

    protected open var value0: V? = null
        get() {
            return field ?: supplier?.let { it().also { field = it } }
        }
        set(value) {
            if (finalization == Finalization.FINAL) {
                throw IllegalStateException("Property has already been read")
            }

            field = value
        }
    open var value: V
        get() {
            if (finalization == Finalization.ENABLED) {
                finalization = Finalization.FINAL
            }

            return value0 ?: throw NullPointerException("Property has not been initialized")
        }
        set(value) {
            value0 = value
        }

    constructor() : this(null)

    constructor(value: V?) {
        value0 = value
        supplier = null
    }

    constructor(supplier: () -> V) {
        this.supplier = supplier
    }

    override fun invoke() = value

    open fun map(func: UnaryOperator<V>) = Property { func.apply(value) }

    open fun finalizeOnRead(): Property<V> {
        if (finalization == Finalization.NONE) {
            finalization = Finalization.ENABLED
        }

        return this
    }

    open fun transmuteIfSet(func: UnaryOperator<V>) {
        value0?.let { value0 = func.apply(it) }
    }

    open fun transmute(func: Function<V?, V>) {
        value0 = func.apply(value0)
    }
}