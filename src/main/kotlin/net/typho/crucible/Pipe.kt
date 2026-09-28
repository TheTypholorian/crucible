package net.typho.crucible

import net.typho.misc_util.EventGraph

class Pipe<V : Any> : Cacheable {
    @JvmField
    val filters = EventGraph<String, FilterInstance<*, V>>()

    @JvmName("filter")
    operator fun invoke(value: V): V {
        return filters.resolve().fold(value) { accum, event -> event.event.filter(accum) }
    }

    override fun canReuse(old: Cacheable): Boolean {
        if (old !is Pipe<V>) {
            return false
        }

        val other = old.filters.events
        val self = filters.resolve()

        if (other.size != self.size) {
            return false
        }

        return self.zip(other).all { (new, old) -> new.id == old.id && Cacheable.canReuse(new.event.parameters, old.event.parameters) }
    }

    data class FilterInstance<P : Any, V : Any>(
        @JvmField
        val parameters: P,
        @JvmField
        val filter: Filter<P, V>
    ) {
        fun filter(value: V) = filter.filter(parameters, value)
    }

    interface Filter<P : Any, V : Any> {
        val parameterClass: Class<P>

        fun filter(parameters: P, value: V): V
    }
}