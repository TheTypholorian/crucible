package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.ide.data.Dependency
import net.typho.crucible.ide.data.DependencyPathType
import java.nio.file.Paths

class Dependencies(
    private val repositories: Repositories
) : ArrayList<Dependency>() {
    val classpath: Classpath
        get() = Classpath(flatMap { it.paths.filter { it.type == DependencyPathType.BINARY }.map { Paths.get(it.path) } })

    fun add(name: DependencyName) = add(repositories.find(name) ?: throw DependencyNotFoundException("Cannot find dependency '${name.coordinates}', searched in repositories:\n\t${Crucible.repositories.joinToString(separator = "\n\t")}"))

    fun add(
        group: String,
        artifact: String,
        version: String
    ) = add(DependencyName("$group:$artifact:$version"))

    fun add(coordinates: String) = add(DependencyName(coordinates))

    fun kotlin(module: String, version: String = Crucible.kotlinVersion) = add(DependencyName("org.jetbrains.kotlin:kotlin-$module:$version"))
}