package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEModuleDependencyData(
    @JvmField
    val ownerModule: IDEModuleData,
    @JvmField
    val target: IDEModuleData,
    @JvmField
    val scope: IDEDependencyScope?,
    @JvmField
    val exported: Boolean?,
    @JvmField
    val order: Int?,

    @JvmField
    val productionOnTestDependency: Boolean?,
    @JvmField
    val moduleDependencyArtifacts: List<Path>,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.MODULE_DEPENDENCY

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEModuleDependencyData> {
            field(IDEModuleDependencyData::moduleDependencyArtifacts, Codec.PATH.listOf().optional(listOf()))
            field(IDEModuleDependencyData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}