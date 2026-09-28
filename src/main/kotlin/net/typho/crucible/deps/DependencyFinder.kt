package net.typho.crucible.deps

import net.typho.crucible.Config
import net.typho.crucible.ILog
import net.typho.crucible.LOG
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.transfer.ArtifactNotFoundException
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import java.net.URI
import java.nio.file.Path

interface DependencyFinder {
    fun find(dep: DependencyCoordinates): Path?

    class Maven(
        @JvmField
        val repositories: List<RemoteRepository>
    ) : DependencyFinder, TransferListener {
        private val system = RepositorySystemSupplier().get()
        private val session = system.createSessionBuilder()
            .withLocalRepositoryBaseDirectories(Config.MAVEN_CACHE_FOLDER)
            .withTransferListener(this)
            .build()
        private val downloads = mutableMapOf<String, ILog.StatusUpdater>()

        companion object {
            const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2/"
        }

        constructor(vararg repositories: String) : this(repositories.map { RemoteRepository.Builder(null, "default", it).build() })

        override fun transferInitiated(event: TransferEvent) {
        }

        override fun transferStarted(event: TransferEvent) {
            val uri = URI(event.resource.repositoryUrl.trimEnd('/') + "/" + event.resource.resourceName.trimStart('/'))
            LOG.info("Downloading $uri")
            downloads[event.resource.resourceName] = when {
                event.resource.contentLength > (1 shl 30) -> LOG.status("gb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000_000f)
                event.resource.contentLength > (1 shl 20) -> LOG.status("mb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000f)
                event.resource.contentLength > (1 shl 10) -> LOG.status("kb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000f)
                else -> LOG.status("bytes", max = event.resource.contentLength.toFloat())
            }
        }

        override fun transferProgressed(event: TransferEvent) {
            downloads[event.resource.resourceName]!!.progress = event.transferredBytes.toFloat()
        }

        override fun transferCorrupted(event: TransferEvent) {
            downloads.remove(event.resource.resourceName)!!.finish()
        }

        override fun transferSucceeded(event: TransferEvent) {
            downloads.remove(event.resource.resourceName)!!.finish()
        }

        override fun transferFailed(event: TransferEvent) {
            downloads.remove(event.resource.resourceName)!!.finish()
        }

        override fun find(dep: DependencyCoordinates): Path? {
            return try {
                system.resolveArtifact(session, ArtifactRequest(DefaultArtifact(dep.group, dep.artifact, dep.classifier, dep.extension, dep.version), repositories, null)).artifact?.path
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