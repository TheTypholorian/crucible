package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.SourceSetType
import net.typho.misc_util.EventGraph

abstract class SourceSet : EventGraph.SelfAware<String> {
    companion object {
        @JvmField
        val all = EventGraph<String, SourceSet>(
            KotlinSourceSet,
            JavaSourceSet,
            ResourcesSourceSet
        )
    }

    abstract override val id: String
    abstract val type: SourceSetType
    val inputs by Crucible.finalizeOnRead { listOf(Crucible.sourceInputFolder.resolve(id)) }
    val output by Crucible.finalizeOnRead { Crucible.sourceOutputFolder.resolve(id) }

    abstract fun compile()
}