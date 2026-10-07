package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.SourceSetType
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.misc_util.EventGraph
import java.nio.file.Path

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
    @JvmField
    val inputs = ListProperty<Path> { listOf(Crucible.sourceInputFolder().resolve(id)) }
    @JvmField
    val output = Property<Path> { Crucible.sourceOutputFolder().resolve(id) }

    abstract fun compile()
}