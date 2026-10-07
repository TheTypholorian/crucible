package net.typho.crucible.deps

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

fun List<Path>.classpathString() = joinToString(separator = File.pathSeparator) { it.absolutePathString() }