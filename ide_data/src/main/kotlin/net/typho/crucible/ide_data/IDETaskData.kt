package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDETaskData(
    @JvmField
    val name: String,
    @JvmField
    val description: String?,
    @JvmField
    val linkedExternalProjectPath: Path?,
    @JvmField
    val group: String?,
    @JvmField
    val type: String?,
    @JvmField
    val inherited: Boolean?,
    @JvmField
    val isJvm: Boolean?,
    @JvmField
    val isTest: Boolean?,
    @JvmField
    val isJvmTest: Boolean?,

    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.TASK

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDETaskData> {
            field(IDETaskData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}