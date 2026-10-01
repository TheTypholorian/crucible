package net.typho.crucible.deps

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.transfer.ArtifactNotFoundException

interface Repository {
    companion object {
        const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
        const val TYPHO_NET = "https://typho.net/maven"

        @JvmStatic
        fun Iterable<Repository>.find(name: DependencyName): ResolvedDependency? {
            return firstNotNullOfOrNull { it.find(name) }
        }
    }

    fun find(name: DependencyName): ResolvedDependency?

    abstract override fun toString(): String

    class Maven(
        @JvmField
        val repo: RemoteRepository
    ) : Repository {
        constructor(repository: String) : this(RemoteRepository.Builder(null, "default", repository).build())

        override fun find(name: DependencyName): ResolvedDependency? {
            return try {
                MavenCache.system.resolveArtifact(MavenCache.session, ArtifactRequest(DefaultArtifact(name.coordinates), listOf(repo), null)).artifact?.let { ResolvedDependency.Maven(it) }
            } catch (e: ArtifactResolutionException) {
                if (e.result.exceptions.all { it is ArtifactNotFoundException }) {
                    null
                } else {
                    throw e
                }
            }
        }

        override fun toString(): String {
            return repo.url
        }
    }
}