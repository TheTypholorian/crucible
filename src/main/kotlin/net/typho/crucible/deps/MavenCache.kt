package net.typho.crucible.deps

import net.typho.crucible.Config
import net.typho.crucible.ILog
import net.typho.crucible.LOG
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import java.net.URI

object MavenCache : TransferListener {
    @JvmField
    val system = RepositorySystemSupplier().get()
    @JvmField
    val session = system.createSessionBuilder()
        .withLocalRepositoryBaseDirectories(Config.MAVEN_CACHE_FOLDER)
        .withTransferListener(this)
        .build()
    @JvmField
    val downloadStatus = mutableMapOf<String, ILog.StatusUpdater>()

    override fun transferInitiated(event: TransferEvent) {
    }

    override fun transferStarted(event: TransferEvent) {
        val uri = URI(event.resource.repositoryUrl.trimEnd('/') + "/" + event.resource.resourceName.trimStart('/'))
        LOG.info("Downloading $uri")
        downloadStatus[event.resource.resourceName] = when {
            event.resource.contentLength > (1 shl 30) -> LOG.status("gb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000_000f)
            event.resource.contentLength > (1 shl 20) -> LOG.status("mb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000_000f)
            event.resource.contentLength > (1 shl 10) -> LOG.status("kb", max = event.resource.contentLength.toFloat(), scale = 1 / 1_000f)
            else -> LOG.status("bytes", max = event.resource.contentLength.toFloat())
        }
    }

    override fun transferProgressed(event: TransferEvent) {
        downloadStatus[event.resource.resourceName]!!.progress = event.transferredBytes.toFloat()
    }

    override fun transferCorrupted(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)!!.finish()
    }

    override fun transferSucceeded(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)!!.finish()
    }

    override fun transferFailed(event: TransferEvent) {
        downloadStatus.remove(event.resource.resourceName)!!.finish()
    }
}