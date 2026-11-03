package net.typho.crucible.source

import net.typho.crucible.SimpleModule
import net.typho.crucible.ide_data.IDESourceType
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

open class ResourcesSourceType(
    module: SimpleModule
) : SourceType(module) {
    override val id = "resources"
    override val type: IDESourceType = IDESourceType.RESOURCE

    @OptIn(ExperimentalPathApi::class)
    override fun compile(all: List<SourceType>, previous: List<SourceType>) {
        output().deleteRecursively()
        output().createDirectories()

        inputs.forEach {
            if (it.exists()) {
                it.copyToRecursively(output(), followLinks = true, overwrite = true)
            }
        }
    }
}