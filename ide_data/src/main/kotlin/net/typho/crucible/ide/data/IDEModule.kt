package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEModule(
    @JvmField
    val name: String,
    @JvmField
    val path: Path,
    @JvmField
    val roots: List<IDEContentRoot>,
    @JvmField
    val dependencies: List<IDEDependency>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEModule> {
            field(IDEModule::roots, IDEContentRoot.CODEC.listOf())
            field(IDEModule::dependencies, IDEDependency.CODEC.listOf())
        }
    }
}