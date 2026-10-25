package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.Lazy
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.ide.data.IDEDependency
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.wrapper.DependencyName
import java.nio.file.Path
import kotlin.io.path.Path

class Dependencies(
    private val repositories: Repositories
) : ArrayList<IDEDependency>() {
    fun getPaths(type: DependencyPathType) = flatMap { it.paths.filter { it.type == type }.map { Path(it.path) } }

    fun add(name: DependencyName) = add(repositories.find(name) ?: throw DependencyNotFoundException("Cannot find dependency '${name.coordinates}', searched in repositories:\n\t${Crucible.repositories.joinToString(separator = "\n\t")}"))

    fun add(
        group: String,
        artifact: String,
        version: String
    ) = add(DependencyName("$group:$artifact:$version"))

    fun add(coordinates: String) = add(DependencyName(coordinates))

    fun kotlin(module: String, version: String = "2.4.21") = add(DependencyName("org.jetbrains.kotlin:kotlin-$module:$version"))
}