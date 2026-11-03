package net.typho.crucible

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class NamedContainer<V : Named, D : V>(
    @JvmField
    protected val defaultConstructor: (id: String) -> D
) : Map<String, V> {
    @JvmField
    protected var map = mutableMapOf<String, V>()

    override val entries: Set<Map.Entry<String, V>>
        get() = map.entries
    override val keys: Set<String>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: Collection<V>
        get() = map.values

    override fun containsKey(key: String) = map.containsKey(key)

    override fun containsValue(value: V) = map.containsValue(value)

    override fun get(key: String) = map[key]

    override fun isEmpty() = map.isEmpty()

    @JvmOverloads
    fun <R : V> register(value: R, config: R.() -> Unit = {}): R {
        map.put(value.id, value)?.let { old ->
            throw IllegalStateException("Entry '${value.id}' is already registered as $old")
        }

        config(value)
        return value
    }

    @JvmOverloads
    fun register(id: String, config: D.() -> Unit = {}) = register(defaultConstructor(id), config)

    open inner class Delegate(
        protected var config: D.() -> Unit
    ) {
        operator fun provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ReadWriteProperty<Any?, D> {
            var holder = register(property.name, config)

            return object : ReadWriteProperty<Any?, D> {
                override fun getValue(thisRef: Any?, property: KProperty<*>) = holder

                override fun setValue(thisRef: Any?, property: KProperty<*>, value: D) {
                    holder = value
                }
            }
        }
    }

    @JvmOverloads
    fun register(config: D.() -> Unit = {}) = Delegate(config)
}