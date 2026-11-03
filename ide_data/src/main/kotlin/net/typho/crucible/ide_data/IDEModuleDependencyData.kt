package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEModuleDependencyData(
    @JvmField
    val ownerModule: IDEModuleData,
    @JvmField
    val target: IDEModuleData,
    @JvmField
    val scope: IDEDependencyScope? = null,
    @JvmField
    val exported: Boolean? = null,
    @JvmField
    val order: Int? = null,

    @JvmField
    val productionOnTestDependency: Boolean? = null,
    @JvmField
    val moduleDependencyArtifacts: List<Path> = listOf(),
    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.MODULE_DEPENDENCY

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEModuleDependencyData> {
            field(IDEModuleDependencyData::ownerModule, IDEModuleData.CODEC)
            field(IDEModuleDependencyData::target, IDEModuleData.CODEC)
            field(IDEModuleDependencyData::moduleDependencyArtifacts, Codec.PATH.listOf().optional(listOf()))
            field(IDEModuleDependencyData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}