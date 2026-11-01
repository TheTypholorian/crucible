package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.ide.data.IDEDependency
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.maven_util.ArtifactCoords

class Dependencies(
    private val repositories: Repositories
) : ArrayList<IDEDependency>() {
    fun getPaths(type: DependencyPathType) = flatMap { it.paths.filter { it.type == type }.map { it.path } }

    fun add(name: ArtifactCoords): Boolean {
        return add(repositories.findIDE(name).orThrow().artifact)
    }

    fun add(
        groupId: String,
        artifactId: String,
        version: String? = null
    ) = add(ArtifactCoords(
        groupId,
        artifactId,
        version ?: Crucible.fallbackDependencyVersions["$groupId:$artifactId"] ?: throw NullPointerException("No version specified for $groupId:$artifactId, and no fallback version specified in ${Crucible.dependencyVersionsFile()}")
    ))

    fun kotlin(module: String, version: String? = null) = add(
        groupId = "org.jetbrains.kotlin",
        artifactId = "kotlin-$module",
        version = version
    )
}