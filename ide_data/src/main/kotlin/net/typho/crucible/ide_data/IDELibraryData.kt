package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDELibraryData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String? = null,

    @JvmField
    val paths: Map<IDELibraryPathType, List<Path>>,

    @JvmField
    val unresolved: Boolean? = null,
    @JvmField
    val groupId: String? = null,
    @JvmField
    val artifactId: String? = null,
    @JvmField
    val version: String? = null,
    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.LIBRARY

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDELibraryData> {
            field(IDELibraryData::paths, Codec.dynamicMap(
                { IDELibraryPathType.valueOf(it) },
                { it.name },
                Codec.PATH.listOf()
            ).optional(mapOf()))
            field(IDELibraryData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}