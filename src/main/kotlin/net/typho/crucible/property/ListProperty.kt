package net.typho.crucible.property

import java.util.function.UnaryOperator

open class ListProperty<V : Any> : Property<List<V>>, MutableList<V> {
    @JvmField
    protected var safeMutable = false
    override var value0: List<V>?
        get() = super.value0
        set(value) {
            safeMutable = false
            super.value0 = value
        }

    constructor() : super()

    constructor(vararg values: V) : this(listOf(*values))

    constructor(value: List<V>?) : super(value)

    constructor(supplier: () -> List<V>) : super(supplier)

    protected fun <R> util(op: MutableList<V>.() -> R): R {
        val list = if (safeMutable) {
            value0 as MutableList<V>
        } else {
            value0?.toMutableList() ?: mutableListOf()
        }

        val r = op(list)

        value0 = list
        safeMutable = true

        return r
    }

    override fun map(func: UnaryOperator<List<V>>) = ListProperty { func.apply(value) }

    override fun finalizeOnRead() = super.finalizeOnRead() as ListProperty<V>

    override val size: Int
        get() = value.size

    override fun get(index: Int) = value[index]

    override fun listIterator() = util { listIterator() }

    override fun listIterator(index: Int) = util { listIterator(index) }

    override fun subList(fromIndex: Int, toIndex: Int) = util { subList(fromIndex, toIndex) }

    override fun isEmpty() = value.isEmpty()

    override fun contains(element: V) = value.contains(element)

    override fun iterator() = util { iterator() }

    override fun containsAll(elements: Collection<V>) = value.containsAll(elements)

    override fun indexOf(element: V) = value.indexOf(element)

    override fun lastIndexOf(element: V) = value.lastIndexOf(element)

    override fun add(element: V) = util { add(element) }

    override fun add(index: Int, element: V) = util { add(index, element) }

    override fun addAll(elements: Collection<V>) = util { addAll(elements) }

    override fun addAll(index: Int, elements: Collection<V>) = util { addAll(index, elements) }

    override fun clear() = util { clear() }

    override fun remove(element: V) = util { remove(element) }

    override fun removeAll(elements: Collection<V>) = util { removeAll(elements) }

    override fun removeAt(index: Int): V = util { removeAt(index) }

    override fun retainAll(elements: Collection<V>) = util { retainAll(elements) }

    override fun set(index: Int, element: V): V = util { set(index, element) }
}