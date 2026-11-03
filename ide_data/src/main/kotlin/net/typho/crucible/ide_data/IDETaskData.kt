package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDETaskData(
    @JvmField
    val name: String,
    @JvmField
    val description: String? = null,
    @JvmField
    val linkedExternalProjectPath: Path,
    @JvmField
    val group: String? = null,
    @JvmField
    val type: String? = null,
    @JvmField
    val inherited: Boolean? = null,
    @JvmField
    val isJvm: Boolean? = null,
    @JvmField
    val isTest: Boolean? = null,
    @JvmField
    val isJvmTest: Boolean? = null,

    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.TASK

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDETaskData> {
            field(IDETaskData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}