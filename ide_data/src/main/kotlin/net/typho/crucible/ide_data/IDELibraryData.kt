package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDELibraryData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String?,

    @JvmField
    val paths: Map<IDELibraryPathType, List<Path>>,

    @JvmField
    val unresolved: Boolean,
    @JvmField
    val groupId: String?,
    @JvmField
    val artifactId: String?,
    @JvmField
    val version: String?,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
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