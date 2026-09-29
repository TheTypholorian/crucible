package net.typho.crucible.deps

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.transfer.ArtifactNotFoundException
import java.nio.file.Path

interface Repository {
    companion object {
        const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
        const val TYPHO_NET = "https://typho.net/maven"

        @JvmStatic
        fun Iterable<Repository>.find(dep: DependencyCoordinates): Path? {
            return firstNotNullOfOrNull { it.find(dep) }
        }
    }

    fun find(dep: DependencyCoordinates): Path?

    abstract override fun toString(): String

    class Maven(
        @JvmField
        val repo: RemoteRepository
    ) : Repository {
        constructor(repository: String) : this(RemoteRepository.Builder(null, "default", repository).build())

        override fun find(dep: DependencyCoordinates): Path? {
            return try {
                MavenCache.system.resolveArtifact(MavenCache.session, ArtifactRequest(DefaultArtifact(dep.group, dep.artifact, dep.classifier, dep.extension, dep.version), listOf(repo), null)).artifact?.path
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