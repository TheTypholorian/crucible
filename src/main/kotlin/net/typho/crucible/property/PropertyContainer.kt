package net.typho.crucible.property

import net.typho.data_util.codec.Codec
import java.util.function.Supplier
import kotlin.jvm.java
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class PropertyContainer(
    internal val propertyPath: String? = null
) {
    var properties: Map<String, Property<*>> = mapOf()
        internal set

    class PropertyDelegate<V : Any, P : Property<V>>(
        private var supplier: (name: String) -> P
    ) {
        operator fun provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ReadOnlyProperty<Any?, P> {
            val property = supplier(property.name)
            return ReadOnlyProperty { thisRef, kProperty -> property }
        }

        fun apply(func: P.() -> Unit): PropertyDelegate<V, P> {
            val old = supplier
            supplier = { name -> old(name).apply(func) }
            return this
        }

        fun set(value: V) = apply { set(value) }

        fun fallback(value: V?) = apply { fallback(value) }

        fun set(property: Property<out V>) = apply { set(property) }

        fun fallback(property: Property<out V>) = apply { fallback(property) }

        fun setLazy(supplier: Supplier<V?>) = apply { setLazy(supplier) }

        fun fallbackLazy(supplier: Supplier<V?>) = apply { fallbackLazy(supplier) }
    }

    open fun getPropertyRef(name: String) = properties[name] ?: throw NullPointerException("Property '$name' does not exist in container '$this'")

    open fun getPropertyValue(name: String) = getPropertyRef(name).get()

    open fun setPropertyValue(name: String, value: Any?) = getPropertyRef(name).setCast(value)

    @Suppress("UNCHECKED_CAST")
    inline fun <reified V : Any> getPropertyCodec() = Codec.getDefaultCodec(V::class.java) as? Codec<V>

    inline fun <reified V : Any> property() = property(getPropertyCodec<V>())

    inline fun <reified V : Any> property(value: V) = property(getPropertyCodec<V>(), value)

    inline fun <reified V : Any> lazyProperty(noinline value: () -> V) = lazyProperty(getPropertyCodec<V>(), value)

    inline fun <reified V : Any> listProperty() = listProperty(getPropertyCodec<V>())

    inline fun <reified V : Any> listProperty(value: List<V>) = listProperty(getPropertyCodec<V>(), value)

    inline fun <reified V : Any> listProperty(vararg values: V) = listProperty<V>(getPropertyCodec<V>(), *values)

    inline fun <reified V : Any> lazyListProperty(noinline value: () -> List<V>) = lazyListProperty(getPropertyCodec<V>(), value)

    inline fun <reified V : Any> lazyListProperty(vararg values: () -> V) = lazyListProperty(getPropertyCodec<V>(), *values)

    fun <V : Any> property(codec: Codec<V>? = null) = PropertyDelegate<V, Property<V>> { propertyNamed(codec, it) }

    fun <V : Any> property(codec: Codec<V>? = null, value: V) = PropertyDelegate<V, Property<V>> { propertyNamed(codec, it, value) }

    fun <V : Any> lazyProperty(codec: Codec<V>? = null, value: () -> V) = PropertyDelegate<V, Property<V>> { lazyPropertyNamed(codec, it, value) }

    fun <V : Any> listProperty(codec: Codec<V>? = null) = PropertyDelegate<List<V>, ListProperty<V>> { listPropertyNamed(codec, it) }

    fun <V : Any> listProperty(codec: Codec<V>? = null, value: List<V>) = PropertyDelegate<List<V>, ListProperty<V>> { listPropertyNamed(codec, it, value) }

    fun <V : Any> listProperty(codec: Codec<V>? = null, vararg values: V) = PropertyDelegate<List<V>, ListProperty<V>> { listPropertyNamed(codec, it, *values) }

    fun <V : Any> lazyListProperty(codec: Codec<V>? = null, value: () -> List<V>) = PropertyDelegate<List<V>, ListProperty<V>> { lazyListPropertyNamed(codec, it, value) }

    fun <V : Any> lazyListProperty(codec: Codec<V>? = null, vararg values: () -> V) = PropertyDelegate<List<V>, ListProperty<V>> { lazyListPropertyNamed(codec, it, *values) }

    private fun <V : Any> propertyNamed(codec: Codec<V>? = null, name: String) = Property(name, codec, this)

    private fun <V : Any> propertyNamed(codec: Codec<V>? = null, name: String, value: V) = Property(name, codec, this).also { it.set(value) }

    private fun <V : Any> lazyPropertyNamed(codec: Codec<V>? = null, name: String, value: () -> V) = Property(name, codec, this).also { it.setLazy(value) }

    private fun <V : Any> listPropertyNamed(codec: Codec<V>? = null, name: String) = ListProperty(name, codec?.listOf(), this)

    private fun <V : Any> listPropertyNamed(codec: Codec<V>? = null, name: String, value: List<V>) = ListProperty(name, codec?.listOf(), this).also { it.set(value) }

    private fun <V : Any> listPropertyNamed(codec: Codec<V>? = null, name: String, vararg values: V) = listPropertyNamed(codec, name, listOf(*values))

    private fun <V : Any> lazyListPropertyNamed(codec: Codec<V>? = null, name: String, value: () -> List<V>) = ListProperty(name, codec?.listOf(), this).also { it.setLazy(value) }

    private fun <V : Any> lazyListPropertyNamed(codec: Codec<V>? = null, name: String, vararg values: () -> V) = lazyListPropertyNamed(codec, name) { values.map { it() } }
}