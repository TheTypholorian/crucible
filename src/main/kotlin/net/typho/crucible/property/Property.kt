package net.typho.crucible.property

import java.util.function.Consumer
import java.util.function.Supplier
import java.util.function.UnaryOperator
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Property<V> : Consumer<V>, Supplier<V>, ReadWriteProperty<Any?, V> {
    protected enum class Finalization {
        NONE,
        ENABLED,
        FINAL
    }

    protected interface Value<V> {
        val value: V

        fun map(func: UnaryOperator<V>): Value<V> = LazyValue { func.apply(value) }
    }

    protected open class SimpleValue<V>(
        override val value: V
    ) : Value<V>

    protected open class LazyValue<V>(
        supplier: Supplier<V>
    ) : Value<V> {
        override val value: V by lazy { supplier.get() }
    }

    @JvmField
    protected var finalization = Finalization.NONE

    protected open var value0: Value<V>? = null
        set(value) {
            if (finalization == Finalization.FINAL) {
                throw IllegalStateException("Property has already been read")
            }

            field = value
        }

    constructor()

    constructor(value: V) : this(SimpleValue(value))

    constructor(supplier: Supplier<V>) : this(LazyValue(supplier))

    protected constructor(value: Value<V>) {
        value0 = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = set(value)

    protected open fun getHolderOrThrow() = value0 ?: throw NullPointerException("Property has not been initialized")

    open fun hasValue() = value0 != null

    override fun get(): V {
        if (finalization == Finalization.ENABLED) {
            finalization = Finalization.FINAL
        }

        return getHolderOrThrow().value
    }

    open operator fun invoke() = get()

    open fun set(value: V) {
        value0 = SimpleValue(value)
    }

    open operator fun invoke(value: V) {
        set(value)
    }

    override fun accept(value: V) {
        set(value)
    }

    open fun setLazy(supplier: Supplier<V>) {
        value0 = LazyValue(supplier)
    }

    open fun transmute(op: UnaryOperator<V>) {
        value0 = getHolderOrThrow().map(op)
    }

    open fun map(func: UnaryOperator<V>) = Property<V> { func.apply(get()) }

    open fun finalizeOnRead(): Property<V> {
        if (finalization == Finalization.NONE) {
            finalization = Finalization.ENABLED
        }

        return this
    }
}