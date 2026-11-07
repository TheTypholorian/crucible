package net.typho.crucible

import net.typho.crucible.ide_data.IDEModuleData
import net.typho.crucible.property.PropertyContainer
import java.nio.file.Path

abstract class Module : PropertyContainer(), Named {
    val buildFolder by lazyProperty { Crucible.buildFolder().resolve(id) }
    val sourceInputFolder by lazyProperty { Crucible.sourceInputFolder().resolve(id) }
    val sourceOutputFolder by lazyProperty { buildFolder().resolve("src") }
    val jarOutputFolder by lazyProperty { buildFolder().resolve("jars") }

    open fun taskId(id: String) = if (this.id == "main") id else this.id + id.replaceFirstChar { it.uppercase() }

    abstract fun toIDE(): IDEModuleData

    abstract fun getClasspath(): List<Path>
}