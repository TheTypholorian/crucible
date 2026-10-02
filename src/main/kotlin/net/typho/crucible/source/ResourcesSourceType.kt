package net.typho.crucible.source

import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists

object ResourcesSourceType : SourceType() {
    override val id = "resources"
    override val type = Type.RESOURCES

    @OptIn(ExperimentalPathApi::class)
    override fun compile() {
        inputs.forEach {
            if (it.exists()) {
                it.copyToRecursively(output, followLinks = true, overwrite = true)
            }
        }
    }
}