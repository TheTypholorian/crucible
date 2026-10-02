package net.typho.crucible.deps

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@JvmInline
value class Classpath(val entries: List<Path>) {
    constructor() : this(listOf())

    operator fun plus(other: Classpath) = plus(other.entries)

    operator fun plus(paths: Collection<Path>) = Classpath(entries + paths)

    operator fun plus(path: Path) = Classpath(entries + listOf(path))

    override fun toString(): String {
        return entries.joinToString(separator = File.pathSeparator) { it.absolutePathString() }
    }
}