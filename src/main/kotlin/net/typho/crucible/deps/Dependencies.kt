package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.Lazy
import net.typho.crucible.deps.Repository.Companion.find
import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.ide.data.Dependency
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.DependencyName
import java.nio.file.Path
import kotlin.io.path.Path

class Dependencies(
    private val repositories: Repositories
) : ArrayList<Lazy<Dependency>>() {
    val paths: List<Path>
        get() = this().flatMap { it.paths.filter { it.type == DependencyPathType.BINARY }.map { Path(it.path) } }

    @JvmName("get")
    operator fun invoke() = map { it() }

    fun add(dependency: () -> Dependency) = add(Lazy(dependency))

    fun add(name: DependencyName) = add { repositories.find(name) ?: throw DependencyNotFoundException("Cannot find dependency '${name.coordinates}', searched in repositories:\n\t${Crucible.repositories.joinToString(separator = "\n\t")}") }

    fun add(
        group: String,
        artifact: String,
        version: String
    ) = add(DependencyName("$group:$artifact:$version"))

    fun add(coordinates: String) = add(DependencyName(coordinates))

    fun kotlin(module: String, version: String = CrucibleWrapper.KOTLIN_VERSION) = add(DependencyName("org.jetbrains.kotlin:kotlin-$module:$version"))
}