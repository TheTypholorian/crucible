package net.typho.crucible.source

import net.typho.crucible.ide.data.SourceSetType
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

open class ResourcesSourceSet : SourceSet() {
    override val id = "resources"
    override val type = SourceSetType.RESOURCES

    @OptIn(ExperimentalPathApi::class)
    override fun compile(all: List<SourceSet>, previous: List<SourceSet>) {
        output().deleteRecursively()
        output().createDirectories()

        inputs.forEach {
            if (it.exists()) {
                it.copyToRecursively(output(), followLinks = true, overwrite = true)
            }
        }
    }

    object Main : ResourcesSourceSet()
}