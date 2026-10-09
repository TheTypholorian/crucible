package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.*
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.supplier.SessionBuilderSupplier
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import java.net.URI
import kotlin.io.path.Path

object MavenCache : TransferListener {
    @JvmField
    val system = RepositorySystemSupplier().get()
    @JvmField
    val session = SessionBuilderSupplier(system).get()
        .withLocalRepositoryBaseDirectories(Path(System.getProperty("user.home"), ".crucible", "caches", "maven"))
        .withTransferListener(this)
        .build()
    @JvmField
    val downloadStatus = mutableMapOf<String, StatusUpdater>()

    override fun transferInitiated(event: TransferEvent) {
    }

    override fun transferStarted(event: TransferEvent) {
        val uri = URI(event.resource.repositoryUrl.trimEnd('/') + "/" + event.resource.resourceName.trimStart('/'))
        info("Downloading $uri")

        if (event.resource.contentLength != -1L) {
            downloadStatus[event.resource.resourceName] = when {
                event.resource.contentLength > (1 shl 30) -> status("gb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000_000f)
                event.resource.contentLength > (1 shl 20) -> status("mb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000f)
                event.resource.contentLength > (1 shl 10) -> status("kb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000f)
                else -> status("bytes", max = event.resource.contentLength.toFloat())
            }
        }
    }

    override fun transferProgressed(event: TransferEvent) {
        downloadStatus[event.resource.resourceName]?.progress = event.transferredBytes.toFloat()
    }

    override fun transferCorrupted(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)?.finish()
    }

    override fun transferSucceeded(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)?.finish()
    }

    override fun transferFailed(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)?.finish()
    }
}