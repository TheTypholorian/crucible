package net.typho.crucible.property

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class PropertyContainer {
    var properties: Map<String, Property<*>> = mapOf()
        internal set

    class PropertyDelegate<P : Property<*>>(
        private var supplier: (name: String) -> P
    ) {
        operator fun provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ReadOnlyProperty<Any?, P> {
            val property = supplier(property.name)
            return ReadOnlyProperty { thisRef, kProperty -> property }
        }

        fun apply(func: P.() -> Unit): PropertyDelegate<P> {
            val old = supplier
            supplier = { name -> old(name).apply(func) }
            return this
        }

        fun finalizeOnRead() = apply { finalizeOnRead() }
    }

    open fun getPropertyRef(name: String) = properties[name] ?: throw NullPointerException("Property '$name' does not exist in container '$this'")

    open fun getPropertyValue(name: String) = getPropertyRef(name).get()

    open fun setPropertyValue(name: String, value: Any?) = getPropertyRef(name).setCast(value)

    fun <V> property() = PropertyDelegate<Property<V>> { property(it) }

    fun <V> property(value: V) = PropertyDelegate<Property<V>> { property(it, value) }

    fun <V> lazyProperty(value: () -> V) = PropertyDelegate<Property<V>> { lazyProperty(it, value) }

    fun <V> listProperty() = PropertyDelegate<ListProperty<V>> { listProperty(it) }

    fun <V> listProperty(value: List<V>) = PropertyDelegate<ListProperty<V>> { listProperty(it, value) }

    fun <V> listProperty(vararg values: V) = PropertyDelegate<ListProperty<V>> { listProperty(it, *values) }

    fun <V> lazyListProperty(value: () -> List<V>) = PropertyDelegate<ListProperty<V>> { lazyListProperty(it, value) }

    fun <V> lazyListProperty(vararg values: () -> V) = PropertyDelegate<ListProperty<V>> { lazyListProperty(it, *values) }

    fun <V> property(name: String) = Property<V>(name, this)

    fun <V> property(name: String, value: V) = property<V>(name).also { it.set(value) }

    fun <V> lazyProperty(name: String, value: () -> V) = property<V>(name).also { it.setLazy(value) }

    fun <V> listProperty(name: String) = ListProperty<V>(name, this)

    fun <V> listProperty(name: String, value: List<V>) = listProperty<V>(name).also { it.set(value) }

    fun <V> listProperty(name: String, vararg values: V) = listProperty<V>(name, listOf(*values))

    fun <V> lazyListProperty(name: String, value: () -> List<V>) = listProperty<V>(name).also { it.setLazy(value) }

    fun <V> lazyListProperty(name: String, vararg values: () -> V) = lazyListProperty<V>(name) { values.map { it() } }
}