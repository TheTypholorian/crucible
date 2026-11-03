package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEProjectData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String? = null,

    @JvmField
    val ideProjectFileDirectoryPath: Path,
    @JvmField
    val linkedExternalProjectPath: Path,
    @JvmField
    val ideGrouping: String? = null,
    @JvmField
    val group: String? = null,
    @JvmField
    val version: String? = null,
    @JvmField
    val description: String? = null,
    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.PROJECT

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEProjectData> {
            field(IDEProjectData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}