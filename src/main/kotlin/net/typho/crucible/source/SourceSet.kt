package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.LOG
import net.typho.crucible.ide.data.SourceSetType
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.misc_util.EventGraph
import java.nio.file.Path
import kotlin.io.path.absolutePathString

abstract class SourceSet : EventGraph.SelfAware<String> {
    companion object {
        @JvmField
        val all = EventGraph<String, SourceSet>(
            KotlinSourceSet.Main,
            JavaSourceSet.Main,
            ResourcesSourceSet.Main
        )
    }

    abstract override val id: String
    abstract val type: SourceSetType
    @JvmField
    val inputs = ListProperty<Path> { listOf(Crucible.sourceInputFolder().resolve(id)) }
    @JvmField
    val output = Property<Path> { Crucible.sourceOutputFolder().resolve(id) }

    open operator fun invoke(all: List<SourceSet>, previous: List<SourceSet>): Path {
        LOG.debug("Compiling source set '$id' to ${output().absolutePathString()}")
        compile(all, previous)
        return output()
    }

    protected abstract fun compile(all: List<SourceSet>, previous: List<SourceSet>)
}