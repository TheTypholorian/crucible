package net.typho.crucible.source

import net.typho.crucible.Config
import net.typho.crucible.deps.Classpath
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
    val inputs by Config.finalizeOnRead { listOf(Config.sourceInputFolder.resolve(id)) }
    val output by Config.finalizeOnRead { Config.sourceOutputFolder.resolve(id) }

    abstract fun compile()

    enum class Type {
        CODE,
        RESOURCES,
        OTHER
    }
}