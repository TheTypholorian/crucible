package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEContentRootData(
    @JvmField
    val data: Map<IDESourceType, List<SourceRoot>>,
    @JvmField
    val rootPath: String,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.CONTENT_ROOT

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEContentRootData> {
            field(IDEContentRootData::data, Codec.dynamicMap(
                { IDESourceType.valueOf(it) },
                { it.name },
                SourceRoot.CODEC.listOf()
            ).optional(mapOf()))
            field(IDEContentRootData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }

    data class SourceRoot(
        @JvmField
        val path: Path,
        @JvmField
        val packagePrefix: String
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<SourceRoot>()
        }
    }
}