package net.typho.crucible.deps

import net.typho.crucible.ide.data.Dependency
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.wrapper.DependencyName
import net.typho.crucible.wrapper.MavenCache
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.transfer.ArtifactNotFoundException
import kotlin.io.path.absolutePathString

interface Repository {
    companion object {
        const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
        const val TYPHO_NET = "https://typho.net/maven"

        @JvmStatic
        fun Iterable<Repository>.find(name: DependencyName): Dependency? {
            return firstNotNullOfOrNull { it.find(name) }
        }
    }

    fun find(name: DependencyName): Dependency?

    abstract override fun toString(): String

    class Maven(
        @JvmField
        val repo: RemoteRepository // TODO
    ) : Repository {
        constructor(repository: String) : this(RemoteRepository.Builder(repository, "default", repository).build())

        fun find(artifact: Artifact): Artifact? {
            return try {
                MavenCache.system.resolveArtifact(MavenCache.session, ArtifactRequest(artifact, listOf(repo), null)).artifact
            } catch (e: ArtifactResolutionException) {
                if (e.result.exceptions.all { it is ArtifactNotFoundException }) {
                    null
                } else {
                    throw e
                }
            }
        }

        override fun find(name: DependencyName): Dependency? {
            val request = DefaultArtifact(name.coordinates)
            val main = find(request) ?: return null
            val sources = find(DefaultArtifact(request.groupId, request.artifactId, "sources", request.extension, request.version))
            return Dependency(name.coordinates, buildList {
                add(Dependency.Path(DependencyPathType.BINARY, main.path.absolutePathString()))

                if (sources != null) {
                    add(Dependency.Path(DependencyPathType.SOURCE, sources.path.absolutePathString()))
                }
            })
        }

        override fun toString(): String {
            return repo.url
        }
    }
}