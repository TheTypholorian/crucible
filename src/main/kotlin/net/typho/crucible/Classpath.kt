package net.typho.crucible

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@JvmInline
value class Classpath(val entries: List<String>) {
    operator fun plus(path: String) = Classpath(entries + path)

    operator fun plus(path: Path) = plus(path.absolutePathString())

    override fun toString(): String {
        return entries.joinToString(separator = File.pathSeparator)
    }
}