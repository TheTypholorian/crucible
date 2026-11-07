package net.typho.crucible.property

import net.typho.crucible.Named
import net.typho.crucible.wrapper.PropertyManager
import net.typho.data_util.codec.Codec
import net.typho.data_util.properties.PropertiesFormat
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.function.UnaryOperator
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Property<V : Any>(
    override val id: String,
    @JvmField
    val codec: Codec<V>?,
    @JvmField
    val container: PropertyContainer
) : Named, Consumer<V>, Supplier<V>, ReadWriteProperty<Any?, V> {
    protected enum class Finalization {
        NONE,
        ENABLED,
        FINAL
    }

    protected interface Value<V : Any> {
        val value: V?

        fun map(func: UnaryOperator<V>): Value<V> = LazyValue { value?.let { func.apply(it) } }

        fun withFallback(value: Value<V>): Value<V>
    }

    protected open class SimpleValue<V : Any>(
        override val value: V?
    ) : Value<V> {
        override fun withFallback(value: Value<V>) = if (this.value == null) value else this
    }

    protected open class LazyValue<V : Any>(
        supplier: Supplier<V?>
    ) : Value<V> {
        override val value: V? by lazy { supplier.get() }

        override fun withFallback(value: Value<V>) = LazyValue { this.value ?: value.value }
    }

    @JvmField
    protected var hasBeenRead = false

    protected open var value0: Value<V>? = null
        set(value) {
            if (hasBeenRead) {
                throw IllegalStateException("Property $container.'$id' has already been read and cannot be modified")
            }

            field = value
        }

    init {
        if (container.properties.containsKey(id)) {
            throw IllegalStateException("Property '$id' already exists in container $container")
        }

        container.properties += id to this

        if (codec != null) {
            setLazy {
                val input = PropertiesFormat.createInput(PropertyManager.properties)
                var value: V? = null
                input.readStaticMap(listOf("${container.propertyPath}.$id")) { input ->
                    value = codec.optional().read(input)
                }
                value
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = set(value)

    protected open fun getHolderOrThrow() = value0 ?: throw NullPointerException("Property '$id' has not been initialized")

    open fun hasValue() = value0 != null

    override fun get(): V {
        hasBeenRead = true

        return value0?.value ?: throw NullPointerException("Property '$id' has not been initialized")
    }

    open operator fun invoke() = get()

    @Suppress("UNCHECKED_CAST")
    fun setCast(value: Any?) {
        set(value as V)
    }

    open fun set(value: V) {
        value0 = SimpleValue(value)
    }

    open fun fallback(value: V?) {
        val value1 = SimpleValue(value)
        value0 = value0?.withFallback(value1) ?: value1
    }

    open operator fun invoke(value: V) {
        set(value)
    }

    override fun accept(value: V) {
        set(value)
    }

    open fun set(property: Property<out V>) {
        setLazy { property() }
    }

    open fun fallback(property: Property<out V>) {
        fallbackLazy { property() }
    }

    open fun setLazy(supplier: Supplier<V?>) {
        value0 = LazyValue(supplier)
    }

    open fun fallbackLazy(supplier: Supplier<V?>) {
        val value1 = LazyValue(supplier)
        value0 = value0?.withFallback(value1) ?: value1
    }

    open fun transmute(op: UnaryOperator<V>) {
        value0 = getHolderOrThrow().map(op)
    }

    override fun toString(): String {
        return value0?.let { it.value?.toString() } ?: "Uninitialized"
    }
}