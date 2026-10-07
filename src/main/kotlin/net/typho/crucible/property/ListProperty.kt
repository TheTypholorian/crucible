package net.typho.crucible.property

import java.util.function.Supplier
import java.util.function.UnaryOperator

open class ListProperty<V> : Property<List<V>>, MutableCollection<V> {
    constructor() : this(listOf())

    constructor(vararg values: V) : this(listOf(*values))

    constructor(value: List<V>) : super(value)

    constructor(supplier: Supplier<List<V>>) : super(supplier)

    override fun transmute(op: UnaryOperator<List<V>>) {
        value0 = value0?.map(op) ?: LazyValue { op.apply(listOf()) }
    }

    override fun map(func: UnaryOperator<List<V>>) = ListProperty { func.apply(get()) }

    override fun finalizeOnRead() = super.finalizeOnRead() as ListProperty<V>

    override val size: Int
        get() = get().size

    override fun isEmpty() = get().isEmpty()

    override fun contains(element: V) = get().contains(element)

    override fun iterator() = object : MutableIterator<V> {
        val iterator = get().iterator()

        override fun remove() {
            throw UnsupportedOperationException()
        }

        override fun hasNext() = iterator.hasNext()

        override fun next() = iterator.next()
    }

    override fun containsAll(elements: Collection<V>) = get().containsAll(elements)

    override fun add(element: V): Boolean {
        transmute { it + element }
        return true
    }

    open fun add(element: Supplier<V>): Boolean {
        transmute { it + element.get() }
        return true
    }

    override fun addAll(elements: Collection<V>): Boolean {
        val elements = elements.toList()
        transmute { it + elements }
        return true
    }

    fun addAll(elements: Supplier<Collection<V>>): Boolean {
        transmute { it + elements.get() }
        return true
    }

    override fun clear() {
        set(listOf())
    }

    override fun remove(element: V): Boolean {
        transmute { it - element }
        return true
    }

    override fun removeAll(elements: Collection<V>): Boolean {
        transmute { it - elements.toSet() }
        return true
    }

    override fun retainAll(elements: Collection<V>): Boolean {
        transmute { it.filter { it in elements } }
        return true
    }
}