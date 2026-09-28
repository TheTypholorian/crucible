package net.typho.crucible.deps

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.transfer.ArtifactNotFoundException
import java.nio.file.Path

interface DependencyFinder : AutoCloseable {
    companion object {
        const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2/"
    }

    fun find(dep: DependencyCoordinates): Path?

    override fun close() {
    }

    class Maven(
        @JvmField
        val repositories: List<RemoteRepository>
    ) : DependencyFinder {
        constructor(vararg repositories: String) : this(repositories.map { RemoteRepository.Builder(null, "default", it).build() })

        override fun find(dep: DependencyCoordinates): Path? {
            return try {
                MavenCache.system.resolveArtifact(MavenCache.session, ArtifactRequest(DefaultArtifact(dep.group, dep.artifact, dep.classifier, dep.extension, dep.version), repositories, null)).artifact?.path
            } catch (e: ArtifactResolutionException) {
                if (e.result.exceptions.all { it is ArtifactNotFoundException }) {
                    null
                } else {
                    throw e
                }
            }
        }
    }
}