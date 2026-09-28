package net.typho.crucible

interface Cacheable {
    fun canReuse(old: Cacheable): Boolean {
        return this == old
    }

    companion object {
        @JvmStatic
        fun canReuse(new: Any, old: Any): Boolean {
            return if (new is Cacheable && old is Cacheable) new.canReuse(old) else new == old
        }
    }
}