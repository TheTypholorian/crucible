package net.typho.crucible

import net.typho.misc_util.EventGraph

class Pipe<V : Any> : Cacheable<Pipe<V>> {
    @JvmField
    val filters = EventGraph<String, Filter<*, V>>()

    @JvmName("filter")
    operator fun invoke(value: V): V {
        return filters.resolve().fold(value) { accum, event -> event.event.filter(accum) }
    }

    override fun canReuse(old: Pipe<V>): Boolean {
        val other = old.filters.events
        val self = filters.resolve()

        if (other.size != self.size) {
            return false
        }

        return self.zip(other).all { (a, b) -> a.id == b.id && a.event.parameters.canReuseCast(b.event.parameters) }
    }

    interface Filter<P : Cacheable<P>, V : Any> {
        val parameters: P

        fun filter(value: V): V
    }
}