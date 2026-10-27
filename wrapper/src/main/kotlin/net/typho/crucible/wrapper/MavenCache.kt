package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.CrucibleClassLoader.addLibrary
import net.typho.crucible.wrapper.log.*
import net.typho.data_util.DataReadException
import net.typho.data_util.impl.JsonFormat
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.supplier.SessionBuilderSupplier
import org.eclipse.aether.transfer.ArtifactNotFoundException
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object MavenCache : TransferListener {
    val system by lazy {
        info("> Initializing maven stuff")
        error(RuntimeException())
        RepositorySystemSupplier().get()
    }
    val session by lazy {
        SessionBuilderSupplier(system).get()
            .withLocalRepositoryBaseDirectories(CrucibleWrapper.mavenCacheFolder)
            .setConfigProperty("aether.remoteRepositoryFilter.prefixes", false)
            .withTransferListener(this)
            .build()
    }
    @JvmField
    val lookupFile = CrucibleWrapper.mavenCacheFolder.resolve("lookup.json")
    @Suppress("UNCHECKED_CAST")
    var lookup: Map<DependencyName, List<Path>> = if (lookupFile.exists()) {
        try {
            (JsonFormat.read(lookupFile.readText()) as Map<String, List<String>>)
                .mapKeys { (key, value) -> DependencyName(key) }
                .mapValues { (key, value) -> value.map { Path(it) } }
        } catch (e: RuntimeException) {
            mapOf()
        }
    } else {
        mapOf()
    }
        set(value) {
            field = value
            lookupFile.writeText(JsonFormat.write(
                value.mapKeys { (key, value) -> key.toString() }
                    .mapValues { (key, value) -> value.map { it.absolutePathString() } }
            ))
        }
    @JvmField
    val repositories = mutableMapOf<String, RemoteRepository>()

    @JvmStatic
    fun resolveArtifact(name: DependencyName, repositories: List<String>): List<Path> {
        if (CrucibleWrapper.hardCaching) {
            lookup[name]?.let { return it }
        }

        try {
            val request = CollectRequest()
            request.repositories = repositories.map { this.repositories.computeIfAbsent(it) { RemoteRepository.Builder(it, "default", it).build() } }
            request.dependencies = listOf(Dependency(name.toArtifact(), "runtime"))

            val dependencies = system.collectDependencies(session, request)
            val paths = system.resolveDependencies(session, DependencyRequest(dependencies.root, null))
                .artifactResults
                .map { it.artifact.path }

            lookup += name to paths
            return paths
        } catch (e: ArtifactResolutionException) {
            if (e.result.exceptions.all { it is ArtifactNotFoundException }) {
                return listOf()
            } else {
                throw e
            }
        }
    }

    override fun transferInitiated(event: TransferEvent) {
    }

    override fun transferStarted(event: TransferEvent) {
        val uri = URI(event.resource.repositoryUrl.trimEnd('/') + "/" + event.resource.resourceName.trimStart('/'))
        val bytes = event.resource.contentLength

        if (bytes != -1L) {
            val template = "Downloading $uri (%.1f %s)"

            when {
                bytes > (1 shl 30) -> info(template.format(bytes.toFloat() / 1_000_000_000f, "gb"))
                bytes > (1 shl 20) -> info(template.format(bytes.toFloat() / 1_000_000f, "mb"))
                bytes > (1 shl 10) -> info(template.format(bytes.toFloat() / 1_000f, "kb"))
                else -> info(template.format(bytes.toFloat(), "bytes"))
            }
        } else {
            info("Downloaded $uri")
        }
    }

    override fun transferProgressed(event: TransferEvent) {
    }

    override fun transferCorrupted(event: TransferEvent) {
    }

    override fun transferSucceeded(event: TransferEvent) {
    }

    override fun transferFailed(event: TransferEvent) {
    }
}