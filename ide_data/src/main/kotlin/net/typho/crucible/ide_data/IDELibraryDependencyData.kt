package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDELibraryDependencyData(
    @JvmField
    val ownerModule: IDEModuleData,
    @JvmField
    val target: IDELibraryData,
    @JvmField
    val scope: IDEDependencyScope?,
    @JvmField
    val exported: Boolean?,
    @JvmField
    val order: Int?,

    @JvmField
    val level: IDELibraryLevel,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.LIBRARY_DEPENDENCY

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDELibraryDependencyData> {
            field(IDELibraryDependencyData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}