package net.typho.crucible.deps

import net.typho.crucible.error.DependencyNotFoundException
import net.typho.crucible.ide.data.IDEDependency
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.wrapper.DependencyName
import kotlin.io.path.Path

class Dependencies(
    private val repositories: Repositories,
    private val fallbackDependencyVersions: Map<String, String>
) : ArrayList<IDEDependency>() {
    fun getPaths(type: DependencyPathType) = flatMap { it.paths.filter { it.type == type }.map { Path(it.path) } }

    fun applyFallbackVersion(name: DependencyName): DependencyName {
        return if (name.version == null) {
            fallbackDependencyVersions[name.toString(withVersion = false)]?.let { name.copy(version = it) }
                ?: fallbackDependencyVersions[name.toString(withVersion = false, withClassifier = false)]?.let { name.copy(version = it) }
                ?: fallbackDependencyVersions[name.toString(withVersion = false, withClassifier = false, withArtifact = false)]?.let { name.copy(version = it) }
                ?: name
        } else {
            name
        }
    }

    fun add(name: DependencyName): Boolean {
        val name = applyFallbackVersion(name)
        return add(repositories.findIDE(name) ?: throw DependencyNotFoundException("Cannot find dependency '${name.coordinates}', searched in repositories:\n\t${repositories.joinToString(separator = "\n\t")}"))
    }

    fun add(
        group: String,
        artifact: String,
        version: String
    ) = add(DependencyName("$group:$artifact:$version"))

    fun add(coordinates: String) = add(DependencyName(coordinates))

    fun kotlin(module: String, version: String? = null) = add(DependencyName(
        groupId = "org.jetbrains.kotlin",
        artifactId = "kotlin-$module",
        version = version
    ))
}