package net.typho.crucible.source

import net.typho.crucible.Module
import net.typho.crucible.Named
import net.typho.crucible.ide.data.SourceSetType
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.wrapper.log.debug
import net.typho.misc_util.EventGraph
import java.nio.file.Path
import kotlin.io.path.absolutePathString

abstract class SourceType(
    @JvmField
    val module: Module
) : PropertyContainer(), EventGraph.SelfAware<String>, Named {
    abstract override val id: String
    abstract val type: SourceSetType
    val inputs by lazyListProperty<Path> { listOf(module.sourceInputFolder().resolve(id)) }
    val output by lazyProperty<Path> { module.sourceOutputFolder().resolve(id) }

    open operator fun invoke(all: List<SourceType>, previous: List<SourceType>): Path {
        debug("Compiling source type '$id' to ${output().absolutePathString()}")
        compile(all, previous)
        return output()
    }

    protected abstract fun compile(all: List<SourceType>, previous: List<SourceType>)
}