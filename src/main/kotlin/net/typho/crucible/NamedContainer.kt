package net.typho.crucible

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class NamedContainer<V : Named>(
    @JvmField
    protected val defaultConstructor: (id: String) -> V
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
    fun register(id: String, config: V.() -> Unit = {}) = register(defaultConstructor(id), config)

    open inner class Delegate(
        protected var config: V.() -> Unit
    ) {
        operator fun provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ReadWriteProperty<Any?, V> {
            var holder = register(property.name, config)

            return object : ReadWriteProperty<Any?, V> {
                override fun getValue(thisRef: Any?, property: KProperty<*>) = holder

                override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
                    holder = value
                }
            }
        }
    }

    @JvmOverloads
    fun register(config: V.() -> Unit = {}) = Delegate(config)
}