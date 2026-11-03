package net.typho.crucible

import net.typho.crucible.ide_data.IDEModuleData
import net.typho.crucible.property.PropertyContainer
import java.nio.file.Path

abstract class Module : PropertyContainer(), Named {
    val buildFolder by lazyProperty { Crucible.buildFolder().resolve(id) }.finalizeOnRead()
    val sourceInputFolder by lazyProperty { Crucible.sourceInputFolder().resolve(id) }.finalizeOnRead()
    val sourceOutputFolder by lazyProperty { buildFolder().resolve("src") }.finalizeOnRead()
    val jarOutputFolder by lazyProperty { buildFolder().resolve("jars") }.finalizeOnRead()

    open fun taskId(id: String) = if (this.id == "main") id else this.id + id.replaceFirstChar { it.uppercase() }

    abstract fun toIDE(): IDEModuleData

    abstract fun getClasspath(): List<Path>
}