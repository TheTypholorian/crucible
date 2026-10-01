package net.typho.crucible.source

import net.typho.crucible.Config
import java.nio.file.Path

interface SourceType {
    companion object {
        @JvmStatic
        val ALL = mutableSetOf<SourceType>()

        @JvmStatic
        val SourceType.output: Path
            get() = Config.PROJECT_ROOT
    }

    val name: String
    val type: Type

    enum class Type {
        CODE,
        RESOURCES,
        OTHER
    }
}