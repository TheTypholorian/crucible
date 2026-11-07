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

    fun <V> property() = PropertyDelegate<Property<V>> { propertyNamed(it) }

    fun <V> property(value: V) = PropertyDelegate<Property<V>> { propertyNamed(it, value) }

    fun <V> lazyProperty(value: () -> V) = PropertyDelegate<Property<V>> { lazyPropertyNamed(it, value) }

    fun <V> listProperty() = PropertyDelegate<ListProperty<V>> { listPropertyNamed(it) }

    fun <V> listProperty(value: List<V>) = PropertyDelegate<ListProperty<V>> { listPropertyNamed(it, value) }

    fun <V> listProperty(vararg values: V) = PropertyDelegate<ListProperty<V>> { listPropertyNamed(it, *values) }

    fun <V> lazyListProperty(value: () -> List<V>) = PropertyDelegate<ListProperty<V>> { lazyListPropertyNamed(it, value) }

    fun <V> lazyListProperty(vararg values: () -> V) = PropertyDelegate<ListProperty<V>> { lazyListPropertyNamed(it, *values) }

    fun <V> propertyNamed(name: String) = Property<V>(name, this)

    fun <V> propertyNamed(name: String, value: V) = propertyNamed<V>(name).also { it.set(value) }

    fun <V> lazyPropertyNamed(name: String, value: () -> V) = propertyNamed<V>(name).also { it.setLazy(value) }

    fun <V> listPropertyNamed(name: String) = ListProperty<V>(name, this)

    fun <V> listPropertyNamed(name: String, value: List<V>) = listPropertyNamed<V>(name).also { it.set(value) }

    fun <V> listPropertyNamed(name: String, vararg values: V) = listPropertyNamed<V>(name, listOf(*values))

    fun <V> lazyListPropertyNamed(name: String, value: () -> List<V>) = listPropertyNamed<V>(name).also { it.setLazy(value) }

    fun <V> lazyListPropertyNamed(name: String, vararg values: () -> V) = lazyListPropertyNamed<V>(name) { values.map { it() } }
}