package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.misc_util.EventGraph

abstract class SourceType : EventGraph.SelfAware<String> {
    companion object {
        @JvmField
        val all = EventGraph<String, SourceType>(
            KotlinSourceType,
            JavaSourceType,
            ResourcesSourceType
        )
    }

    abstract override val id: String
    abstract val type: Type
    val inputs by Crucible.finalizeOnRead { listOf(Crucible.sourceInputFolder.resolve(id)) }
    val output by Crucible.finalizeOnRead { Crucible.sourceOutputFolder.resolve(id) }

    abstract fun compile()

    enum class Type {
        CODE,
        RESOURCES,
        OTHER
    }
}