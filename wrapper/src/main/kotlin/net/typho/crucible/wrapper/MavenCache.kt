package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.*
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.supplier.SessionBuilderSupplier
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import java.net.URI

object MavenCache : TransferListener {
    @JvmField
    val system = RepositorySystemSupplier().get()
    @JvmField
    val session = SessionBuilderSupplier(system).get()
        .withLocalRepositoryBaseDirectories(CrucibleWrapper.mavenCacheFolder)
        .withTransferListener(this)
        .build()

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