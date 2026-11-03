package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec

data class IDELibraryDependencyData(
    @JvmField
    val ownerModule: IDEModuleData,
    @JvmField
    val target: IDELibraryData,
    @JvmField
    val scope: IDEDependencyScope? = null,
    @JvmField
    val exported: Boolean? = null,
    @JvmField
    val order: Int? = null,

    @JvmField
    val level: IDELibraryLevel,
    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.LIBRARY_DEPENDENCY

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDELibraryDependencyData> {
            field(IDELibraryDependencyData::ownerModule, IDEModuleData.CODEC)
            field(IDELibraryDependencyData::target, IDELibraryData.CODEC)
            field(IDELibraryDependencyData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}