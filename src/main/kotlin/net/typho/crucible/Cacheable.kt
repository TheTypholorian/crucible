package net.typho.crucible

interface Cacheable<S : Cacheable<S>> {
    @Suppress("UNCHECKED_CAST")
    fun canReuseCast(old: Cacheable<*>): Boolean {
        return try {
            canReuse(old as S)
        } catch (e: ClassCastException) {
            false
        }
    }

    fun canReuse(old: S): Boolean {
        return this == old
    }
}