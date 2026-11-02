package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEProjectData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String?,

    @JvmField
    val ideProjectFileDirectoryPath: Path,
    @JvmField
    val linkedExternalProjectPath: Path,
    @JvmField
    val ideGrouping: String?,
    @JvmField
    val group: String?,
    @JvmField
    val version: String?,
    @JvmField
    val description: String?,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.PROJECT

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEProjectData> {
            field(IDEProjectData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}