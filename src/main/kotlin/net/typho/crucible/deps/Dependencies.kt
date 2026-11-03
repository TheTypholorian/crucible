package net.typho.crucible.deps

import net.typho.crucible.Crucible
import net.typho.crucible.ide_data.IDELibraryData
import net.typho.crucible.ide_data.IDELibraryPathType
import net.typho.maven_util.ArtifactCoords

class Dependencies(
    private val repositories: Repositories
) : ArrayList<IDELibraryData>() {
    fun getPaths(type: IDELibraryPathType) = flatMap { it.paths[type] ?: listOf() }

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