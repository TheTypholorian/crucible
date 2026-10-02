package net.typho.crucible.source

import net.typho.crucible.Classpath
import net.typho.crucible.Config
import net.typho.misc_util.EventGraph
import kotlin.io.path.absolutePathString

abstract class SourceType : EventGraph.SelfAware<String> {
    companion object {
        @JvmField
        val all = EventGraph<String, SourceType>(
            KotlinSourceType,
            JavaSourceType,
            ResourcesSourceType
        )
        val allOutputs: Classpath
            get() = Classpath(all.events.map { it.event.output.absolutePathString() })
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